package model;

import java.io.Serializable;

public class BeklemeDugumu implements Serializable {
    private static final long serialVersionUID = 1L;

    public Musteri musteri;
    public BeklemeDugumu sonraki;

    public BeklemeDugumu(Musteri musteri) {
        this.musteri = musteri;
        this.sonraki = null;
    }
}