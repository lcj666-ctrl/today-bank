package com.aipaint.ai;

public class PromptBuilder {

    public static String build(String style) {
        switch (style) {
            case "cartoon":
                return "convert a child's doodle drawing into a cute cartoon illustration, colorful, clean background";
            case "watercolor":
                return "convert a child's doodle drawing into a watercolor painting, soft colors";
            case "3d":
                return "convert a child's doodle drawing into a 3D rendering, realistic lighting, detailed";
            case "pixel":
                return "convert a child's doodle drawing into a pixel art, retro style, vibrant colors";
            case "ghibli":
                return "convert a child's doodle drawing into a Studio Ghibli style illustration, magical, whimsical";
            default:
                return "convert a child's doodle drawing into a beautiful illustration";
        }
    }
}