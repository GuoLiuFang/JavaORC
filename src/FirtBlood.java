import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LiuFangGuo on 5/7/17.
 */
public class FirtBlood {
    private Map<BufferedImage, String> map = new HashMap<>();
    public final static int WHITE_THREHOLD = 100;

    public FirtBlood() throws IOException {
        File trainDir = new File("/Users/LiuFangGuo/Downloads/train");
        File[] fileList = trainDir.listFiles();
        for (File f : fileList) {
            this.map.put(ImageIO.read(f), f.getName().charAt(0) + "");
        }
    }

    /*
    像素点匹配的最多的就是
    其实，这里面有一个默认前提是。。分割后的图片大小和。。样本的大小相同。。
     */
    public String getSingleCharOcr(BufferedImage imge) {
        int width = imge.getWidth();
        int height = imge.getHeight();
        int max = -1;
        String singleResult = "";
        for (Map.Entry<BufferedImage, String> entry : this.map.entrySet()) {
            BufferedImage sampleImage = entry.getKey();
            int count = 0;
        /*
        接下来一个像素点一个像素点的比较。。样本图片没有二值化。。
         */
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    //如果像素点相同计数加一
                    if (isWhite(imge.getRGB(x, y)) == isWhite(sampleImage.getRGB(x, y))) {
                        count++;
                    }
                }
            }
            //判断与最大值得关系。。
            if (count > max) {
                max = count;
                singleResult = entry.getValue();
            }
        }
        return singleResult;
    }

    //判断颜色是否为白色。。大于100是白色。。。
    public boolean isWhite(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() > FirtBlood.WHITE_THREHOLD) {
            return true;
        }
        return false;
    }

    /*
    去除背景颜色实质上市进行了二值化操作。。。
     */
    public BufferedImage removeBackgroud(String picFile) throws IOException {
        BufferedImage image = ImageIO.read(new File(picFile));
        int width = image.getWidth();
        int height = image.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (isWhite(image.getRGB(x, y))) {
                    image.setRGB(x, y, Color.WHITE.getRGB());
                } else {
                    image.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }
        return image;
    }

    /*
    本来分割图片是一个很难的功能。这里因为是固定大小的所以简化了。
     */
    public List<BufferedImage> splitImage(BufferedImage img) {
        ArrayList<BufferedImage> imageArrayList = new ArrayList<BufferedImage>();
        imageArrayList.add(img.getSubimage(10, 6, 8, 10));
        imageArrayList.add(img.getSubimage(19, 6, 8, 10));
        imageArrayList.add(img.getSubimage(28, 6, 8, 10));
        imageArrayList.add(img.getSubimage(37, 6, 8, 10));
        return imageArrayList;
    }

    /*
    传入一个图片，识别出来图片中，所有的数字。。
     */
    public String getAllOrc(String file) throws IOException {
        /*
        第一步：去除背景色。。
         */
        BufferedImage removeBGImg = removeBackgroud(file);
        /*
        第二步：分割图片。。
         */
        List<BufferedImage> imageList = splitImage(removeBGImg);
        /*
        第四步：对分割后的每个图片，使用像素点与训练数据逐个比对。选择命中最多的为结果。
         */
        String result = "";
        for (BufferedImage bimg :
                imageList) {
            result += getSingleCharOcr(bimg);
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        FirtBlood firtBlood = new FirtBlood();
        for (int i = 0; i < 30; ++i) {
            String contenttext = firtBlood.getAllOrc("/Users/LiuFangGuo/Downloads/img/" + i + ".jpg");
            System.out.println("/Users/LiuFangGuo/Downloads/img/" + i + ".jpg的内容是" + contenttext);
        }

    }
}
