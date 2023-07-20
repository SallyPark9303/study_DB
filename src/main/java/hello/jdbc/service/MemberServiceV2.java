package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import javax.sql.DataSource;
import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@AllArgsConstructor
public class MemberServiceV2 {

    private  final DataSource dataSource;
    private  final MemberRepositoryV2 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();
        try{
            con.setAutoCommit(false); //트랜잭션 시작
            // 비지니스 로직
            bizLogic(con, fromId, toId, money);
            con.commit(); //성공시 커밋


        }catch (Exception e){
            con.rollback(); //실패시 롤백
            throw new IllegalStateException(e);
        }finally {
            release(con);
        }

    }

    private void bizLogic(Connection con, String fromId, String toId, int money)  throws SQLException {
        Member fromMember = memberRepository.findById(con,fromId);
        Member toMember = memberRepository.findById(con,toId);

        memberRepository.update(con, fromId, fromMember.getMoney() -money);
        //validation(toMember);

        memberRepository.update(con,toId,toMember.getMoney()+money);
    }

    private void validation(Member toMember){
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

    /**
     * 커넥션 사용 후 종료. 커넥션 풀을 사용하면 con.close()를 호출했을 때 풀에 반납됨.
     * 현재 수동 커밋 모드 동작하기 때문에 풀에 돌려주기 전에 기본값인 자동 커밋 모드로 변경해야함
     *
     */
    private void release(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true); //커넥션 풀 고려
                con.close();
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }
}
