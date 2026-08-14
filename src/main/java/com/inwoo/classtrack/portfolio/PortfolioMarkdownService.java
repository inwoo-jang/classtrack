package com.inwoo.classtrack.portfolio;

import com.inwoo.classtrack.calendar.AcademicCalendar;
import com.inwoo.classtrack.domain.Assignment;
import com.inwoo.classtrack.domain.AssignmentMode;
import com.inwoo.classtrack.domain.AssignmentRequirement;
import com.inwoo.classtrack.domain.Course;
import com.inwoo.classtrack.repository.AssignmentRepository;
import com.inwoo.classtrack.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 기록을 Markdown 문서로 바꾼다. README 나 이력서에 그대로 붙여넣는 것이 목적이다.
 *
 * <p>설계 원칙: <b>비어 있는 항목은 통째로 건너뛴다.</b> 기록은 늘 부분적으로만 채워지는데,
 * 빈 칸이 "목적: " 처럼 남으면 문서가 지저분해져서 결국 손으로 고치게 된다.
 * 채운 것만 나오면 조금씩 채워도 결과물이 항상 멀쩡하다.
 */
@Service
@Transactional(readOnly = true)
public class PortfolioMarkdownService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;
    private final AcademicCalendar calendar;

    public PortfolioMarkdownService(
            CourseRepository courseRepository,
            AssignmentRepository assignmentRepository,
            AcademicCalendar calendar) {
        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
        this.calendar = calendar;
    }

    /**
     * @param featuredOnly 대표 과제만 담는다. 과제가 하나도 남지 않는 강의는 통째로 빠진다.
     */
    public String render(boolean featuredOnly) {
        Map<Long, List<Assignment>> byCourse = assignmentRepository.findAllByOrderByDueDateAsc()
                .stream()
                .filter(a -> !featuredOnly || a.isFeatured())
                .collect(Collectors.groupingBy(a -> a.getCourse().getId()));

        // 학습 기록이므로 시간 순서대로 읽히는 게 자연스럽다
        List<Course> courses = courseRepository.findAll().stream()
                .sorted(Comparator.comparing(Course::getStartDate).thenComparing(Course::getId))
                .filter(c -> !featuredOnly || !byCourse.getOrDefault(c.getId(), List.of()).isEmpty())
                .toList();

        StringBuilder md = new StringBuilder();
        md.append("# 학습 기록\n\n");

        appendSummary(md, courses, byCourse, featuredOnly);
        appendTechStack(md, courses, byCourse);

        for (Course course : courses) {
            appendCourse(md, course, byCourse.getOrDefault(course.getId(), List.of()));
        }

        return md.toString().stripTrailing() + "\n";
    }

    private void appendSummary(
            StringBuilder md,
            List<Course> courses,
            Map<Long, List<Assignment>> byCourse,
            boolean featuredOnly) {

        long assignmentCount = byCourse.values().stream().mapToLong(List::size).sum();
        long days = courses.stream().mapToLong(Course::getDurationDays).sum();

        md.append("강의 ").append(courses.size()).append("개");
        if (assignmentCount > 0) {
            md.append(" · ").append(featuredOnly ? "대표 과제 " : "과제 ").append(assignmentCount).append("개");
        }
        md.append(" · 수업일 ").append(days).append("일\n\n");
    }

    /** 전체에서 쓰인 기술을 한 번에 보여준다. 이력서 첫 줄에 넣기 좋은 형태. */
    private void appendTechStack(
            StringBuilder md, List<Course> courses, Map<Long, List<Assignment>> byCourse) {

        Set<String> all = new LinkedHashSet<>();
        for (Course course : courses) {
            all.addAll(course.getTechnologies());
            for (Assignment assignment : byCourse.getOrDefault(course.getId(), List.of())) {
                all.addAll(assignment.getTechnologies());
            }
        }
        if (all.isEmpty()) {
            return;
        }

        md.append("## 기술 스택\n\n").append(codeList(all)).append("\n\n");
    }

    private void appendCourse(StringBuilder md, Course course, List<Assignment> assignments) {
        md.append("---\n\n");
        md.append("## ").append(course.getSubject()).append("\n\n");
        md.append("**").append(course.getTitle()).append("**\n\n");

        LocalDate end = calendar.endDateOf(course);
        md.append("- 기간: ").append(course.getStartDate().format(DATE));
        if (!end.equals(course.getStartDate())) {
            md.append(" ~ ").append(end.format(DATE));
        }
        md.append(" (").append(course.getDurationDays()).append("일)\n");

        md.append("- 강사: ").append(course.getInstructor());
        if (course.getPracticeProfessor() != null) {
            md.append(" (실습 ").append(course.getPracticeProfessor()).append(")");
        }
        md.append("\n");

        md.append("- 진행: ").append(course.isLiveLecture() ? "대면" : "비대면").append("\n");

        if (!course.getTechnologies().isEmpty()) {
            md.append("- 다룬 기술: ").append(codeList(course.getTechnologies())).append("\n");
        }
        md.append("\n");

        if (assignments.isEmpty()) {
            return;
        }

        md.append("### 실습\n\n");
        for (Assignment assignment : assignments) {
            appendAssignment(md, assignment);
        }
    }

    private void appendAssignment(StringBuilder md, Assignment assignment) {
        md.append("#### ").append(assignment.getTitle());
        if (assignment.isFeatured()) {
            md.append(" ⭐");
        }
        md.append("\n\n");

        md.append("- 유형: ").append(describeMode(assignment));
        md.append(" · ").append(assignment.getRequirement() == AssignmentRequirement.REQUIRED
                ? "필수" : "자율").append("\n");

        appendDescription(md, assignment.getDescription());

        if (!assignment.getTechnologies().isEmpty()) {
            md.append("- 사용 기술: ").append(codeList(assignment.getTechnologies())).append("\n");
        }

        if (!assignment.getSubmissionLinks().isEmpty()) {
            String links = assignment.getSubmissionLinks().stream()
                    .map(link -> "[" + linkLabel(link.getUrl()) + "](" + link.getUrl() + ")")
                    .collect(Collectors.joining(" · "));
            md.append("- 결과물: ").append(links).append("\n");
        }

        md.append("\n");
    }

    /**
     * 수행 내용은 여러 줄로 적는 경우가 많다. 한 줄로 이어붙이면 읽을 수 없게 되므로
     * 줄이 여럿이면 하위 목록으로 편다. 이미 "-" 로 시작하는 줄은 기호를 떼고 다시 붙인다.
     */
    private static void appendDescription(StringBuilder md, String description) {
        if (description == null || description.isBlank()) {
            return;
        }

        List<String> lines = description.lines()
                .map(line -> line.replaceFirst("^\\s*[-*·•]\\s*", "").strip())
                .filter(line -> !line.isEmpty())
                .toList();

        if (lines.isEmpty()) {
            return;
        }
        if (lines.size() == 1) {
            md.append("- 수행 내용: ").append(lines.get(0)).append("\n");
            return;
        }

        md.append("- 수행 내용:\n");
        for (String line : lines) {
            md.append("  - ").append(line).append("\n");
        }
    }

    private static String describeMode(Assignment assignment) {
        if (assignment.getAssignmentMode() != AssignmentMode.TEAM) {
            return "개인";
        }
        return assignment.getTeamSize() == null
                ? "팀"
                : "팀 (" + assignment.getTeamSize() + "명)";
    }

    /** URL 로만 적으면 링크가 여러 개일 때 뭐가 뭔지 알 수 없다. 호스트로 이름을 붙인다. */
    private static String linkLabel(String url) {
        try {
            String host = java.net.URI.create(url).getHost();
            if (host == null) {
                return "링크";
            }
            host = host.replaceFirst("^www\\.", "");
            if (host.contains("github")) return "GitHub";
            if (host.contains("gitlab")) return "GitLab";
            if (host.contains("google")) return "Drive";
            if (host.contains("notion")) return "Notion";
            return host;
        } catch (IllegalArgumentException e) {
            return "링크";
        }
    }

    private static String codeList(Iterable<String> values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append("`").append(value).append("`");
        }
        return sb.toString();
    }
}
