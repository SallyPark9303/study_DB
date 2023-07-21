package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 템플릿
 */
@Slf4j

public class MemberServiceV3_2 {

    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }


    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        txTemplate.executeWithoutResult((status) ->{  // 메소드안에서 예외 발생시 롤백처리, 정상 작동시 커밋처리한다.
                    try{
                        // 비지니스 로직
                        bizLogic(fromId, toId, money);
                    }catch (Exception e){
                        throw new IllegalStateException(e);
                    } }
                );
    }

    private void bizLogic(String fromId, String toId, int money)  throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() -money);
        //validation(toMember);

        memberRepository.update(toId,toMember.getMoney()+money);
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
