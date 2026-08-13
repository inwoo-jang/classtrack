package com.inwoo.classtrack.calendar;

import com.inwoo.classtrack.domain.Course;
import com.inwoo.classtrack.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CalendarService {

    private final CourseRepository courseRepository;
    private final AcademicCalendar calendar;

    public CalendarService(CourseRepository courseRepository, AcademicCalendar calendar) {
        this.courseRepository = courseRepository;
        this.calendar = calendar;
    }

    /**
     * 기간에 걸치는 수업일을 펼쳐서 돌려준다.
     *
     * <p>강의마다 수업일 목록을 만든 뒤 요청 기간 밖은 걸러낸다. 강의 수가 많지 않으므로
     * 이 정도면 충분하고, 날짜 계산 규칙이 한 곳(AcademicCalendar)에만 있어 어긋날 일이 없다.
     */
    public CalendarResponse getCalendar(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("종료일이 시작일보다 빠릅니다.");
        }

        List<CalendarResponse.Session> sessions = new ArrayList<>();

        for (Course course : courseRepository.findAll()) {
            List<LocalDate> dates =
                    calendar.sessionDates(course.getStartDate(), course.getDurationDays());

            for (int i = 0; i < dates.size(); i++) {
                LocalDate date = dates.get(i);
                if (date.isBefore(from) || date.isAfter(to)) {
                    continue;
                }
                sessions.add(new CalendarResponse.Session(
                        course.getId(),
                        course.getTitle(),
                        course.getSubject(),
                        date,
                        i + 1,
                        dates.size(),
                        course.isLiveLecture()));
            }
        }

        sessions.sort(Comparator
                .comparing(CalendarResponse.Session::date)
                .thenComparing(CalendarResponse.Session::courseTitle));

        List<LocalDate> holidays = calendar.holidaysBetween(from, to).stream().sorted().toList();

        return new CalendarResponse(from, to, holidays, sessions);
    }
}
