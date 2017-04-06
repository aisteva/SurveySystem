package services

import services.EmailService

/**
 * Created by arturas on 2017-04-04.
 */
class EmailServiceTest extends GroovyTestCase {
    void testSendInvitation() {
        EmailService s = new EmailService();
        s.sendEmail("arturasfio@gmail.com", "http://www.google.lt");
    }
}
