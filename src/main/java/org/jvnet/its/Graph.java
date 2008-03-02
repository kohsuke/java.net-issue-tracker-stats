package org.jvnet.its;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class Graph {
    /**
     * Debug method.
     */
    protected Object loadDataset() throws IOException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getDataFile()));
        try {
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } finally {
            ois.close();
        }
    }

    protected void saveDataset(Dataset ds) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getDataFile()));
        oos.writeObject(ds);
        oos.close();
    }

    private File getDataFile() {
        return new File(getClass().getName()+".dataset");
    }

    public void write(JFreeChart chart, File target) throws IOException {
        BufferedImage image = chart.createBufferedImage(640,480);
        FileOutputStream fos = new FileOutputStream(target);
        ImageIO.write(image, "PNG", fos);
        fos.close();
    }
}
