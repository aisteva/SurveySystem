package userModule;

/**
 * Created by arturas on 2017-05-28.
 * Sign in interface
 */
public interface SignInInterface
{
    String signIn();
    String signOut();
    String getPersonFullName();
    void reload();
    String isSigned();
    boolean isAdmin();
    String onlyAdmin();
}
