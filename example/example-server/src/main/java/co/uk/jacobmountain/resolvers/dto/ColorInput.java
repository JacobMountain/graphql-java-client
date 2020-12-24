package co.uk.jacobmountain.resolvers.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("ColorInput")
public class ColorInput {
    private Integer red;

    private Integer green;

    private Integer blue;

    public Integer getRed() {
        return this.red;
    }

    public void setRed(Integer red) {
        this.red = red;
    }

    public Integer getGreen() {
        return this.green;
    }

    public void setGreen(Integer green) {
        this.green = green;
    }

    public Integer getBlue() {
        return this.blue;
    }

    public void setBlue(Integer blue) {
        this.blue = blue;
    }

    public String toString() {
        return "{ ColorInput red: " + this.red + ", green: " + this.green + ", blue: " + this.blue + " }";
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        ColorInput colorInput = (ColorInput) other;
        return Objects.equals(this.red, colorInput.getRed()) &&
                Objects.equals(this.green, colorInput.getGreen()) &&
                Objects.equals(this.blue, colorInput.getBlue());
    }
}
