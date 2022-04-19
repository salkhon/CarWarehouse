package com.carwarehouse;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class Car implements Serializable {
    public long serialVersionUID = 1;

    private final String registrationNumber;
    private int yearMade;
    private int quantity;
    private String color1;
    private String color2;
    private String color3;
    private String maker;
    private String model;
    private int price;
    // prepended with width and height
    private byte[] imageBytes;

    // cache
    private transient WritableImage writableImage;

    public Car(String registrationNumber, int yearMade, int quantity, String color1, String color2, String color3, String maker, String model, int price) {
        this.registrationNumber = registrationNumber.trim();
        this.yearMade = yearMade;
        this.quantity = quantity;
        this.color1 = color1 != null ? color1.trim() : "";
        this.color2 = color2 != null ? color2.trim() : "";
        this.color3 = color3 != null ? color3.trim() : "";
        this.maker = maker.trim();
        this.model = model.trim();
        this.price = price;
    }

    public Car(String registrationNumber, int yearMade, int quantity,
               String color1, String color2, String color3, String maker,
               String model, int price, Image image) {
        this(registrationNumber, yearMade, quantity,
                color1, color2, color3, maker, model, price);
        storeBytesFromImage(image);
    }

    public Car(String registrationNumber, int yearMade, int quantity,
               String color1, String color2, String color3, String maker,
               String model, int price, File imagePath) throws IOException {
        this(registrationNumber, yearMade, quantity,
                color1, color2, color3, maker, model, price,
                imagePath != null ?
                new Image(new ByteArrayInputStream(Files.readAllBytes(imagePath.toPath()))) : null);
    }

    public Car(String registrationNumber, int yearMade, int quantity,
               String color1, String color2, String color3, String maker,
               String model, int price, byte[] imageBytes) {
        this(registrationNumber, yearMade, quantity, color1, color2, color3,
                maker, model, price);
        this.imageBytes = imageBytes;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public int getYearMade() {
        return yearMade;
    }

    public String getColor1() {
        return color1;
    }

    public String getColor2() {
        return color2;
    }

    public String getColor3() {
        return color3;
    }

    public String getMaker() {
        return maker;
    }

    public String getModel() {
        return model;
    }

    public int getPrice() {
        return price;
    }


    public int getQuantity() {
        return quantity;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public WritableImage getImage() {
        WritableImage writableImage = null;
        if (this.imageBytes != null) {
            if (this.writableImage == null) {
                cacheImageBytesToWritableImage();
            }
            writableImage = this.writableImage;
        }
        return writableImage;
    }

    public boolean matchesMakeModel(String make, String model) {
        boolean matches;
        matches = this.maker.equalsIgnoreCase(make);
        if (matches && !model.equalsIgnoreCase("any")) {
            matches = this.model.equalsIgnoreCase(model);
        }
        return matches;
    }

    public boolean isValid() {
        return !this.registrationNumber.isEmpty() &&
                this.yearMade >= 0 &&
                this.quantity > 0 &&
                !this.color1.isEmpty() &&
                !this.maker.isEmpty() && !this.model.isEmpty();
    }

    private void storeBytesFromImage(Image image) {
        if (image != null) {
            int width = (int) image.getWidth();
            byte[] widthBytes = ByteBuffer.allocate(4).putInt(width).array();
            int height = (int) image.getHeight();
            byte[] heightBytes = ByteBuffer.allocate(4).putInt(height).array();

            this.imageBytes = new byte[8 + width * height * 4];

            System.arraycopy(widthBytes, 0, this.imageBytes, 0, widthBytes.length);
            System.arraycopy(heightBytes, 0, this.imageBytes, 4, heightBytes.length);

            image.getPixelReader().getPixels(0, 0, width, height,
                    PixelFormat.getByteBgraInstance(),
                    this.imageBytes, 8, width * 4);
            this.writableImage = new WritableImage(width, height);
            this.writableImage.getPixelWriter().setPixels(0, 0, width, height,
                    PixelFormat.getByteBgraInstance(),
                    this.imageBytes, 8, width * 4);
        }
    }

    private void cacheImageBytesToWritableImage() {
        byte[] widthBytes = new byte[4];
        byte[] lengthBytes = new byte[4];
        System.arraycopy(this.imageBytes, 0, widthBytes, 0, widthBytes.length);
        System.arraycopy(this.imageBytes, widthBytes.length, lengthBytes, 0, lengthBytes.length);
        byte[] actualImageBytes = new byte[this.imageBytes.length - (widthBytes.length + lengthBytes.length)];
        System.arraycopy(this.imageBytes, widthBytes.length + lengthBytes.length,
                actualImageBytes, 0, actualImageBytes.length);

        int width = ByteBuffer.wrap(widthBytes).getInt();
        int height = ByteBuffer.wrap(lengthBytes).getInt();
        this.writableImage = new WritableImage(width, height);
        this.writableImage.getPixelWriter().setPixels(0, 0, width, height,
                PixelFormat.getByteBgraInstance(), actualImageBytes, 0, width * 4);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        boolean equals = false;
        if (obj instanceof Car) {
            Car that = (Car) obj;

            equals = this.registrationNumber.equals(that.registrationNumber) &&
                    this.yearMade == that.yearMade &&
                    this.color1.equalsIgnoreCase(that.color1) && this.color2.equalsIgnoreCase(that.color2) &&
                    this.color3.equalsIgnoreCase(that.color3) && this.maker.equalsIgnoreCase(that.maker) &&
                    this.model.equalsIgnoreCase(that.model) && this.price == that.price;
        }
        return equals;
    }

    public void setEditableFieldsFrom(Car editedCar) {
        this.yearMade = editedCar.yearMade;
        this.quantity = editedCar.quantity;
        this.color1 = editedCar.color1;
        this.color2 = editedCar.color2;
        this.color3 = editedCar.color3;
        this.maker = editedCar.maker;
        this.model = editedCar.model;
        this.price = editedCar.price;
        if (editedCar.imageBytes != null) {
            this.imageBytes = editedCar.imageBytes;
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("\t")
                .append("Registration Number: ").append(this.registrationNumber).append("\n")
                .append("\t").append("Year Made: ").append(this.yearMade).append("\n")
                .append("\t").append("Quantity: ").append(this.quantity).append("\n")
                .append("\t").append("Colors: ");

        if (!this.color1.isEmpty()) {
            stringBuilder.append(this.color1);
        }
        if (!this.color2.isEmpty()) {
            stringBuilder.append(", ").append(this.color2);
        }
        if (!this.color3.isEmpty()) {
            stringBuilder.append(", ").append(this.color3);
        }

        stringBuilder.append("\n").append("\t").append("Maker: ").append(this.maker).append("\n")
                .append("\t").append("Model: ").append(this.model).append("\n")
                .append("\t").append("Price: ").append(this.price).append("\n");
        return stringBuilder.toString();
    }
}
