import org.springframework.lang.Nullable;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Created by maxim on 03.10.18.
 */
public class ChessServerWebAppInitializer
        extends AbstractAnnotationConfigDispatcherServletInitializer
{
    @Override
    protected String[] getServletMappings() {
        return new String[]  {"/"};
    }


    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{RootConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{WebConfig.class, WebSocketConfig.class};
    }
}
