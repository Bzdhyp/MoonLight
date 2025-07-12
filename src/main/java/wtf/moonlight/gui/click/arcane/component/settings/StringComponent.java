
package wtf.moonlight.gui.click.arcane.component.settings;

import org.apache.commons.lang3.math.NumberUtils;
import org.lwjglx.input.Keyboard;
import wtf.moonlight.gui.click.Component;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.module.values.impl.StringValue;
import wtf.moonlight.util.animations.advanced.Animation;
import wtf.moonlight.util.animations.advanced.Direction;
import wtf.moonlight.util.animations.advanced.impl.DecelerateAnimation;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 23:16
 */
public class StringComponent extends Component {
    private final StringValue setting;
    private final Animation input = new DecelerateAnimation(250, 1);
    private boolean inputting;
    private String text = "";
    public StringComponent(StringValue setting) {
        this.setting = setting;
        setHeight(38);
        input.setDirection(Direction.BACKWARDS);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        input.setDirection(inputting ? Direction.FORWARDS : Direction.BACKWARDS);
        text = setting.getValue();
        if (setting.isOnlyNumber() && !NumberUtils.isNumber(text)) {
            text = text.replaceAll("[a-zA-Z]", "");
        }
        String textToDraw = setting.getValue().isEmpty() && !inputting ? "Empty..." : setting.getText();

        Fonts.Bold.get(18).drawString(setting.getName(), getX() + 10, getY() + 4, ColorUtil.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(),0.4f));
        RoundedUtil.drawRound(getX() + 10, getY() + 14, 145, 14, 2, INSTANCE.getArcaneClickGui().smallbackgroundColor2);
        drawTextWithLineBreaks(textToDraw + (inputting && text.length() < 59 && System.currentTimeMillis() % 1000 > 500 ? "|" : ""), getX() + 14, getY() + 18, 90);
        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (RenderUtil.isHovering(getX() + 106,getY() + 13,80,16,mouseX,mouseY) && mouseButton == 0){
            inputting = !inputting;
        } else {
            inputting = false;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (setting.isOnlyNumber() && !NumberUtils.isNumber(String.valueOf(typedChar))) {
            return;
        }
        if (inputting){
            if (keyCode == Keyboard.KEY_BACK) {
                deleteLastCharacter();
            }

            if (text.length() < 18 && (Character.isLetterOrDigit(typedChar) || keyCode == Keyboard.KEY_SPACE)) {
                text += typedChar;
                setting.setText(text);
            }
        }
        super.keyTyped(typedChar, keyCode);
    }
    private void drawTextWithLineBreaks(String text, float x, float y, float maxWidth) {
        String[] lines = text.split("\n");
        float currentY = y;

        for (String line : lines) {
            List<String> wrappedLines = wrapText(line, 6, maxWidth);
            for (String wrappedLine : wrappedLines) {

                Fonts.Bold.get(16).drawString(wrappedLine, x, currentY, ColorUtil.interpolateColor2(new Color(-1).darker(),
                        new Color(-1), (float) input.getOutput()));
                currentY += Fonts.Bold.get(16).getHeight();
            }
        }
    }

    private List<String> wrapText(String text, float size, float maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (Fonts.Bold.get(16).getStringWidth(word) <= maxWidth) {
                if (Fonts.Bold.get(16).getStringWidth(currentLine.toString() + word) <= maxWidth) {
                    currentLine.append(word).append(" ");
                } else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word).append(" ");
                }
            } else {
                if (!currentLine.toString().isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                currentLine = breakAndAddWord(word, currentLine, size, lines);
            }
        }

        if (!currentLine.toString().isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
    private void deleteLastCharacter() {
        if (!text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            setting.setText(text);
        }
    }
    private StringBuilder breakAndAddWord(String word, StringBuilder currentLine, float maxWidth, List<String> lines) {
        int wordLength = word.length();
        for (int i = 0; i < wordLength; i++) {
            char c = word.charAt(i);
            String nextPart = currentLine.toString() + c;
            if (Fonts.Bold.get(16).getStringWidth(nextPart) <= maxWidth) {
                currentLine.append(c);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(String.valueOf(c));
            }
        }
        return currentLine;
    }
    @Override
    public boolean isVisible() {
        return setting.getVisible().get();
    }
}
