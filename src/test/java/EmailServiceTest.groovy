import services.EmailService

/**
 * Created by arturas on 2017-04-04.
 */
class EmailServiceTest extends GroovyTestCase {
    void testSendInvitation() {
        EmailService s = new EmailService();
        s.sendInvitation("arturasfio@gmail.com", "http://www.google.lt");
    }
}
