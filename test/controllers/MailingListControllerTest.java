package controllers;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import daos.InstallationDao;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import play.Application;
import play.Environment;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Call;
import play.mvc.Result;
import play.test.WithApplication;

import static play.test.Helpers.route;
import static utils.TestHelper.fakeRequestWithoutBody;

/**
 * @author rishabh
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.crypto.*", "javax.net.ssl.*"})
public class MailingListControllerTest extends WithApplication {

    private InstallationDao dao;

    @Override
    protected Application provideApplication() {
        final Module testModule = new AbstractModule() {
            @Override
            public void configure() {
                dao = PowerMockito.mock(InstallationDao.class);
                bind(InstallationDao.class).toInstance(dao);
            }
        };
        return new GuiceApplicationBuilder().in(Environment.simple()).overrides(testModule).build();
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(33372);
    //public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicPort());

    @After
    public void tearDown() {
        wireMockRule.stop();
    }

    private void secondsToSleep(int seconds) {
        seconds = (seconds > 10 || seconds < 1) ? 10 : seconds;
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetMailingLists() {
        String installationId = "ebc6a11b9deb6974af6bee7a";
        Call call = routes.MailingListController.getLists();
        Result result = route(fakeRequestWithoutBody(call, "?installationId=" + installationId));

//        Assert.assertEquals(Http.Status.OK, result.status());
//        Assert.assertEquals(Http.MimeTypes.JSON, result.contentType().get());
//        Assert.assertTrue(contentAsString(result).contains("57761079fa7ac9310143dab2"));
    }

    @Test
    public void testGetMailingListsWithoutInstallationId() {
    }

    @Test
    public void testGetMailingListsEmptyInstallationId() {
    }

    @Test
    public void testGetMailingListsWhenInstallationNotFound() {
    }

    @Test
    public void testGetMailingListsWithoutMailChimpAPIDetails() {
    }

}
