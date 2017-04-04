/**
 * Created by arturas on 2017-04-04.
 */
class EmailSessionTest extends GroovyTestCase {
    void testSendInvitation() {
        EmailSession s = new EmailSession();
        s.sendInvitation("arturasfio@gmail.com", "http://www.google.lt");
    }
}
