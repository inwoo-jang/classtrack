package com.inwoo.classtrack.dev;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 엔드포인트가 무슨 일을 하는지 한 줄로 적어둔다. 구현 현황 화면이 이 값을 읽어 보여준다.
 *
 * <p>설명을 코드 옆에 두는 이유: 별도 문서로 관리하면 코드를 고칠 때 같이 안 고쳐져서
 * 금방 거짓말이 된다. 메서드 바로 위에 있으면 눈에 띈다.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiDescription {
    String value();
}
