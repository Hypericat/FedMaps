package me.hypericats.fedmaps.screens;

import me.hypericats.fedmaps.API.SessionAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.session.Session;
import net.minecraft.client.util.Clipboard;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class SSIDScreen extends Screen {
    private static SSIDScreen instance;
    public static Session session;

    public static SSIDScreen getInstance() {
        if (instance == null) instance = new SSIDScreen(null);
        return instance;
    }

    private final Screen parent;
    private TextFieldWidget SSIDField;
    private String feedBackMessage = "";
    private int feedBackColor = 0xFFFFFFFF;
    private int centerX = 0;
    private int centerY = 0;

    private SSIDScreen(Screen parent) {
        super(Text.of("SSID"));
        this.parent = parent;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    protected void init() {
        TextWidget ssidText = new TextWidget(Text.of("SSID"), MinecraftClient.getInstance().textRenderer);

        centerX = this.width / 2 - 50;
        centerY = 60;

        SSIDField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 100, 20, session == null ? Text.empty() : Text.of(session.getSessionId()));
        ssidText.setWidth(100);
        ssidText.setPosition(centerX, centerY + 35);
        SSIDField.setPosition(centerX, centerY + 45);
        SSIDField.setMaxLength(10000);


        this.addDrawableChild(ssidText);
        this.addDrawableChild(SSIDField);
        this.addDrawableChild(ButtonWidget.builder(Text.of("Login"), button -> login()).width(100).position(centerX, centerY + 70).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Reset"), button -> reset()).width(100).position(centerX, centerY + 95).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Copy SSID"), button -> copySSID()).width(100).position(centerX, centerY + 120).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawText(MinecraftClient.getInstance().textRenderer, feedBackMessage, centerX + 50 - (MinecraftClient.getInstance().textRenderer.getWidth(feedBackMessage) >> 1), centerY, feedBackColor, true);
        String currentUser = "Current Account : " + MinecraftClient.getInstance().getSession().getUsername();
        context.drawText(MinecraftClient.getInstance().textRenderer, currentUser, centerX + 50 - (MinecraftClient.getInstance().textRenderer.getWidth(currentUser) >> 1), centerY + 10, 0xFFFFFFFF, true);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    private void login() {
        if (SSIDField.getText().isEmpty()) {
            feedBackMessage = "Please enter an SSID!";
            feedBackColor = 0xFF8f0000;
            return;
        }
        String[] info;
        try {
            info = SessionAPI.getProfileInfo(SSIDField.getText());
        } catch (IOException e) {
            feedBackMessage = "Failed to poll API for username and UUID!";
            feedBackColor = 0xFF8f0000;
            return;
        } catch (Exception e) {
            feedBackMessage = "Invalid SSID!";
            e.printStackTrace();
            feedBackColor = 0xFF8f0000;
            return;
        }
        try {
            session = new Session(info[0], SessionAPI.undashedToUUID(info[1]), SSIDField.getText(), Optional.empty(), Optional.empty());
        } catch (Exception e) {
            feedBackMessage = "Failed to parse UUID from string!";
            feedBackColor = 0xFF8f0000;
            return;
        }

        feedBackMessage = "Successfully updated session!";
        feedBackColor = 0xFF009405;
        return;
    }

    public static void reset() {
        session = null;
    }

    public static void copySSID() {
        MinecraftClient.getInstance().keyboard.setClipboard(MinecraftClient.getInstance().getSession().getAccessToken());
    }
}
