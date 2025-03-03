package src.util;

public class LoadingBar {

    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String UP = "\033[A";
    public static final String DOWN = "\033[B";
    public static final String RIGHT = "\033[C";
    public static final String LEFT = "\033[D";
    public static final String CLEAR = "\033[H\033[J";
    public static final String RESET_CURSOR = "\033[H";
    public static final String HIDE_CURSOR = "\033[?25l";
    public static final String SHOW_CURSOR = "\033[?25h";

    private int width;
    private String loadingGraphic;
    private String padding;
    private char paddingChar;
    private String loading = "";
    private char loadingChar;

    public LoadingBar(int width) {
        this.width = width;
        this.loadingGraphic = "|%s%s|";
        this.padding = " ".repeat(width);
        this.paddingChar = ' ';
        this.loadingChar = '=';
    }

    public LoadingBar(int width, String loadingGraphic) {
        this.width = width;
        this.loadingGraphic = loadingGraphic;
        this.padding = " ".repeat(width);
        this.paddingChar = ' ';
        this.loadingChar = '=';
    }

    public LoadingBar(int width, String loadingGraphic, char padding) {
        this.width = width;
        this.loadingGraphic = loadingGraphic;
        this.padding = String.valueOf(padding).repeat(width);
        this.paddingChar = padding;
        this.loadingChar = '=';
    }

    public LoadingBar(int width, String loadingGraphic, String padding) {
        this.width = width;
        this.loadingGraphic = loadingGraphic;
        this.padding = padding;
        this.loadingChar = '=';
        this.paddingChar = padding.charAt(0);
    }
    
    public LoadingBar(int width, String loadingGraphic, char padding, char loadingChar, char fillerChar, String top, String bottom) {
        this.width = width;
        this.loadingGraphic = loadingGraphic;
        this.padding = String.valueOf(padding).repeat(width);
        this.paddingChar = padding;
        this.loadingChar = loadingChar;
        
        System.out.printf(top, String.valueOf(fillerChar).repeat(width));
        this.show();
        System.out.printf(bottom, String.valueOf(fillerChar).repeat(width));
        System.out.print(LoadingBar.UP + "");
    }

    public void show(){
        System.out.printf("\r" + this.loadingGraphic, this.loading, this.padding);
    }

    public void lerp(int percent){
        int loadingLength = (int) Math.round((percent / 100.0) * this.width);
        this.loading = String.valueOf(this.loadingChar).repeat(loadingLength);
        this.padding = String.valueOf(this.paddingChar).repeat(this.width - loadingLength);
        System.out.printf("\r" + this.loadingGraphic, this.loading, this.padding);
        try { Thread.sleep(5); } catch (InterruptedException e) { }
    }

    public void increase(int times) {
        for (int i = 0; i < times; i++) {
            this.loading += this.loadingChar;
            this.padding = this.padding.substring(1);
            System.out.printf("\r" + this.loadingGraphic, this.loading, this.padding);
        }
    }

    public void increase(int times, int delay){
        for (int i = 0; i < times; i++) {
            this.loading += this.loadingChar;
            this.padding = this.padding.substring(1);
            System.out.printf("\r" + this.loadingGraphic, this.loading, this.padding);
            try { Thread.sleep(delay); } catch (InterruptedException e) { }
        }
    }

    public void increase() {
        this.loading += this.loadingChar;
        this.padding = this.padding.substring(1);
        System.out.printf("\r" + this.loadingGraphic, this.loading, this.padding);
        try { Thread.sleep(25); } catch (InterruptedException e) { }
    }

    public int getWidth() { return this.width; }
}