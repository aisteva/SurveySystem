package services;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 * Created by arturas on 2017-04-22.
 */
@RunWith(Arquillian.class)
public class EmailServiceTest
{
    @Inject
    EmailService es;

    @Test
    public void sendEmail() throws Exception
    {
        es.sendEmail("arturasfio@gmail.com","Labas");
    }

    @Deployment
    public static JavaArchive createDeployment()
    {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(EmailService.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

}
