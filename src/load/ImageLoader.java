package load;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by baislsl on 17-5-15.
 */
public class ImageLoader {
    /**
     * <p>catch image in the internet and return the image data </p>
     *
     * @param url the image website url
     * @param display system display
     * @return SWT Image class
     * */
    public static Image downLoad(String url, Display display) throws ImageCatchException {
        Image image = null;
        try {
            URL uri = new URL(url);
            InputStream in = uri.openStream();
            image = new Image(display, in);
        } catch (Exception e) {
            throw new ImageCatchException(e);
        }
        return image;
    }
}
