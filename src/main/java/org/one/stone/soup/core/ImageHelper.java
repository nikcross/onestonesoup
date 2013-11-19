package org.one.stone.soup.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;
import java.awt.image.RGBImageFilter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImageHelper {

	public static BufferedImage loadImage(File file) throws IOException {
		return ImageIO.read(file);
	}
	
	public static void saveImage(File file,BufferedImage image) throws IOException {
		ImageIO.write(image, "PNG", file);
	}
	
	public static void savePNGImage(File file,BufferedImage image) throws IOException {
		ImageIO.write(image, "PNG", file);
	}
	
	public static void saveBMPImage(File file,BufferedImage image) throws IOException {
		ImageIO.write(image, "BMP", file);
	}
	
	//Reference: http://en.wikipedia.org/wiki/ICO_(file_format)#Icon_resource_structure
	public static void saveICOImage(File file,BufferedImage image) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(image, "PNG", bos);
		
		FileOutputStream out = new FileOutputStream(file);
		out.write(new byte[]{0x0,0x0,0x0,0x1,0x0,0x1});
		

		out.write(new byte[]{
				(byte)image.getWidth(),
				(byte)image.getHeight(),
				0x0,0x0,0x0,0x0,0x0,0x0,
				size
				offset
				});
		out.write(bos.toByteArray());
		
		out.flush();
		out.close();
	}
	
	public static void saveJPEGImage(File file,BufferedImage image) throws IOException {
		ImageIO.write(image, "JPG", file);
	}

	public static Image makeColorTransparent(Image image, Color color) {
		final Color testColor = color;
		RGBImageFilter filter = new RGBImageFilter() {
			// Alpha bits are set to opaque, regardless of what they
			// might have been already.
			public int markerRGB = testColor.getRGB() | 0xFF000000;

			public final int filterRGB(int x, int y, int rgb) {
				if ((rgb | 0xFF000000) == markerRGB) {
					// Mark the alpha bits as zero - transparent,but
					// preserve the other information about the color
					// of the pixel.
					return 0x00FFFFFF & rgb;
				} else {
					// leave the pixel untouched
					return rgb;
				}
			}
		}; // end of inner class

		// Setup to use transparency filter
		ImageProducer ip = new FilteredImageSource(image.getSource(), filter);

		// Pull the old image thru this filter and create a new one
		return Toolkit.getDefaultToolkit().createImage(ip);
	}

	public static Color getColorAt(Image image, int x, int y) {
		int scan = image.getWidth(null);
		int[] data = new int[1];

		PixelGrabber pGrab = new PixelGrabber(image, x, y, x + 1, y + 1, data,
				0, scan);
		try {
			pGrab.grabPixels();
		} catch (Exception e) {
		}

		return new Color(data[0]);
	}

	public static Image resizeImage(BufferedImage source, int width,
			int height, boolean maintainAspectRation) throws IOException {
		if (maintainAspectRation) {
			Dimension bounds = getImageBoundedSizeMaintainingAspectRatio(
					source, width, height);
			width = bounds.width;
			height = bounds.height;
		}

		double scaleX = (double) width / source.getWidth();
		double scaleY = (double) height / source.getHeight();
		AffineTransform scaleTransform = AffineTransform.getScaleInstance(
				scaleX, scaleY);
		AffineTransformOp bilinearScaleOp = new AffineTransformOp(
				scaleTransform, AffineTransformOp.TYPE_BILINEAR);

		return bilinearScaleOp.filter(source, new BufferedImage(width, height,
				source.getType()));
	}

	public static Dimension getImageBoundedSizeMaintainingAspectRatio(
			Image image, int maxWidth, int maxHeight) {
		double width = image.getWidth(null);
		double widthFactor = 1;
		if (width > maxWidth) {
			widthFactor = maxWidth / width;
		}

		double heightFactor = 1;
		double height = image.getHeight(null);
		if (height > maxHeight) {
			heightFactor = maxHeight / height;
		}

		double factor = 1;
		if (widthFactor != 1) {
			factor = widthFactor;
		}
		if (heightFactor < factor) {
			factor = heightFactor;
		}

		Dimension newSize = new Dimension();

		newSize.width = (int) (width * factor);
		newSize.height = (int) (height * factor);

		return newSize;
	}
	
	public static BufferedImage convertToBufferedImage(Image image)
	{
		if(image instanceof BufferedImage) {
			return (BufferedImage)image;
		}
		
		ImageIcon icon = new ImageIcon(image);
        BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(),icon.getIconHeight(),BufferedImage.TYPE_INT_ARGB);
        Graphics grfx = bufferedImage.getGraphics();
        grfx.drawImage(image,0,0,icon.getIconWidth(),icon.getIconHeight(),null);

        return bufferedImage;
	}
}
