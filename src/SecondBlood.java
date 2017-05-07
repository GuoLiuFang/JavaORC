import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LiuFangGuo on 5/7/17.
 */
public class SecondBlood {
    private Map<BufferedImage, String> map = new HashMap<>();
    public final static int WHITE_THREHOLD = 100;

    public SecondBlood() throws IOException {
        File trainDir = new File("/Users/LiuFangGuo/Downloads/train2");
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
            int minwidth = sampleImage.getWidth() > width ? width : sampleImage.getWidth();
            int minheight = sampleImage.getHeight() > height ? height : sampleImage.getHeight();
            int count = 0;
        /*
        接下来一个像素点一个像素点的比较。。样本图片没有二值化。。
         */
            for (int x = 0; x < minwidth; x++) {
                for (int y = 0; y < minheight; y++) {
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
        if (color.getRed() + color.getGreen() + color.getBlue() > SecondBlood.WHITE_THREHOLD) {
            return true;
        }
        return false;
    }

    public BufferedImage removeBlackBlank(BufferedImage imge) {
        int width = imge.getWidth();
        int height = imge.getHeight();
        int start = 0;
        int end = height - 1;
//        在列上的空白已经全部除去。。现在就是把行的空白除去就好。。
        boolean flag = true;
        for (int y = 0; flag && y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isWhite(imge.getRGB(x, y))) {
                    start = y;
                    flag = false;
                    break;
                }
            }
        }
        flag = true;
        for (int y = height - 1; flag && y > -1; y--) {
            for (int x = 0; x < width; x++) {
                if (isWhite(imge.getRGB(x, y))) {
                    end = y;
                    flag = false;
                    break;
                }
            }

        }
        return imge.getSubimage(0, start, width, end - start + 1);
    }

    /*
    去除背景颜色实质上市进行了二值化操作。。。
     */
    public BufferedImage removeBackgroud(String picFile) throws IOException {
        BufferedImage image = ImageIO.read(new File(picFile));
//        因为图片已经进行二值化。。
        return image;
    }

    /*
    本来分割图片是一个很难的功能。这里因为是固定大小的所以简化了。
     */
    public List<BufferedImage> splitImage(BufferedImage img) {
        ArrayList<BufferedImage> imageArrayList = new ArrayList<BufferedImage>();
        ArrayList<Integer> columnInfoList = new ArrayList<>();
        int width = img.getWidth();
        int height = img.getHeight();
        for (int x = 0; x < width; x++) {
            int count = 0;
            for (int y = 0; y < height; y++) {
                if (isWhite(img.getRGB(x, y))) {
                    count++;
                }
            }
            columnInfoList.add(count);
        }
        for (int i = 0; i < columnInfoList.size(); ) {
            int continuousWidth = 0;
            while (columnInfoList.get(i++) > 1) {
                continuousWidth++;
            }
            if (continuousWidth > 12) {
                imageArrayList.add(removeBlackBlank(img.getSubimage(i - 1 - continuousWidth, 0, continuousWidth / 2, height)));
                imageArrayList.add(removeBlackBlank(img.getSubimage(i - 1 - continuousWidth / 2, 0, continuousWidth / 2, height)));
            } else if (continuousWidth > 3) {
                imageArrayList.add(removeBlackBlank(img.getSubimage(i - 1 - continuousWidth, 0, continuousWidth, height)));
            }
        }
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
        for (BufferedImage bimg : imageList) {
            result += getSingleCharOcr(bimg);
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        SecondBlood firtBlood = new SecondBlood();
        for (int i = 0; i < 30; ++i) {
            String contenttext = firtBlood.getAllOrc("/Users/LiuFangGuo/Downloads/img2/" + i + ".jpg");
            System.out.println("/Users/LiuFangGuo/Downloads/img2/" + i + ".jpg的内容是" + contenttext);
        }

    }
}
