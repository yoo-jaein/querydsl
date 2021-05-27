package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em); //em이 멀티스레드 환경에서 동시성 문제 없게 설계되어 있음
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() {
        //member1을 찾아라.
        String qlString = "select m from Member m " +
                        "where m.username = :username"; //런타임 에러

        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1") //파라미터 바인딩을 직접 해줘야
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        //방법 1: QMember m = QMember.member;
        //방법 2: QMember m = new QMember("m"); - 같은 테이블을 조인해야 하는 경우에만

        //방법 3: static import 권장!
        Member findMember = queryFactory
                .select(member) //컴파일 에러
                .from(member)
                .where(member.username.eq("member1")) //파라미터 바인딩을 자동으로 해줌
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }
}