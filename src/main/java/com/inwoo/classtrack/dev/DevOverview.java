package com.inwoo.classtrack.dev;

import java.util.List;

/** 구현 현황 화면에 필요한 것을 한 번에 담는다. */
public record DevOverview(
        List<EndpointInfo> endpoints,
        List<ServiceMethodInfo> serviceMethods,
        List<EntityInfo> entities) {

    /** HTTP 엔드포인트 하나. Spring 의 핸들러 매핑에서 뽑아낸다. */
    public record EndpointInfo(
            /** 리소스 묶음 이름 (courses / assignments / …) */
            String group,
            String httpMethod,
            String path,
            String controller,
            String handler,
            /** {@link ApiDescription} 값. 안 붙였으면 null */
            String description,
            /** 이 핸들러에 붙은 애노테이션 이름들 (@LogExecution 등) */
            List<String> annotations) {
    }

    /** 서비스 메서드 하나. 트랜잭션이 어떻게 걸려 있는지 보여준다. */
    public record ServiceMethodInfo(
            String serviceClass,
            String method,
            boolean transactional,
            boolean readOnly,
            /** LoggingAspect 의 포인트컷에 걸리는지 */
            boolean aopLogged) {
    }

    /** JPA 엔티티 하나. Metamodel 에서 뽑아낸다. */
    public record EntityInfo(
            String name,
            String tableName,
            List<AttributeInfo> attributes) {
    }

    public record AttributeInfo(
            String name,
            String type,
            /** BASIC / MANY_TO_ONE / ONE_TO_MANY ... */
            String kind,
            boolean id,
            boolean optional,
            /** 연관관계면 상대 엔티티 이름, 아니면 null */
            String targetEntity) {
    }
}
