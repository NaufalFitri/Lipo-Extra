package dev.lipoteam.lipoHud;

import dev.lipoteam.lipoHud.Files.Configurations;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import javax.xml.crypto.Data;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BedrockForm {

    private static LipoHud plugin = null;
    private static Configurations configs;
    private static DataManager dataManager;

    private Set<String> ListCForms = new HashSet<>();
    private Set<String> ListSForms = new HashSet<>();
    private static final ConcurrentHashMap<String, Map<String, Map<String, Object>>> CformCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Map<String, Object>> SformCache = new ConcurrentHashMap<>();

    public BedrockForm(Configurations config, LipoHud plugin) {

        BedrockForm.plugin = plugin;
        setConfig(config);
        dataManager = new DataManager(plugin);

    }

    public void setConfig(Configurations config) {

        ListCForms = config.ListCForms();
        ListSForms = config.ListSForms();

        configs = config;

        cacheSimpleForms();
        cacheCustomForms();

    }

    private void cacheCustomForms() {
        CformCache.clear(); // Clear previous cache

        for (String form : ListCForms) {
            Map<String, Map<String, Object>> sectionsCache = new HashMap<>();

            for (String section : configs.ListCFormSections(form)) {
                Map<String, Object> sectionData = new HashMap<>();

                if (section.equalsIgnoreCase("action")) {
                    sectionData.put("type", configs.getSectionSValue(form, section, "type"));
                    sectionData.put("command", configs.getSectionSValue(form, section, "command"));
                    sectionsCache.put(section, sectionData);
                    continue;
                }

                sectionData.put("type", configs.getSectionSValue(form, section, "type"));
                sectionData.put("label", configs.getSectionSValue(form, section, "label"));

                switch (sectionData.get("type").toString().toLowerCase()) {
                    case "dropdown" -> sectionData.put("options", configs.getSectionSValues(form, section, "options"));
                    case "input" -> sectionData.put("placeholder", configs.getSectionSValue(form, section, "placeholder"));
                    case "slider" -> sectionData.put("stepper", configs.getSectionIValues(form, section, "stepper"));
                }

                sectionsCache.put(section, sectionData);
            }

            CformCache.put(form, sectionsCache);
        }
    }

    private void cacheSimpleForms() {
        SformCache.clear(); // Clear previous cache

        for (String form : ListSForms) {
            Map<String, Object> formData = new HashMap<>();

            // Store form content
            formData.put("content", configs.getSValue(form, "content"));

            List<Map<String, String>> buttons = new ArrayList<>();
            Set<String> sections = configs.ListSFormSections(form);

            for (String section : sections) {
                if (section.equalsIgnoreCase("content")) continue;

                Map<String, String> buttonData = new HashMap<>();
                buttonData.put("label", configs.getSSectionSValue(form, section, "label"));
                buttonData.put("action", configs.getSSectionSValue(form, section, "action"));

                String imgType = configs.getSSectionSValue(form, section, "img-type");
                if (!imgType.equalsIgnoreCase("nothing")) {
                    buttonData.put("img-type", imgType);
                    buttonData.put("source", configs.getSSectionSValue(form, section, "source"));
                }

                buttons.add(buttonData);
            }

            formData.put("buttons", buttons);
            SformCache.put(form, formData);
        }
    }

    private static void CreateCustomForms(Player player, String form) {

        Audience console = plugin.adventure().console();
        var mm = MiniMessage.miniMessage();

        CustomForm.Builder builder = CustomForm.builder().title(form);

        Map<String, Map<String, Object>> sections = CformCache.getOrDefault(form, new HashMap<>());

        for (String section : sections.keySet()) {

            if (section.equalsIgnoreCase("action")) {
                continue;
            }

            Map<String, Object> data = sections.get(section);

            String type = (String) data.get("type");
            String label = (String) data.get("label");

            switch (type.toLowerCase()) {
                case "dropdown" -> {
                    List<String> options = new ArrayList<>((List<String>) data.get("options"));
                    if (options.contains("players")) {
                        options.remove("players");
                        Bukkit.getOnlinePlayers().forEach(oplayer -> {
                            String name = oplayer.getName();
                            if (!(name.equals(player.getName()))) {
                                options.add(name);
                            }
                        });
                    }
                    builder.dropdown(label, options);
                }
                case "input" -> builder.input(label, (String) data.getOrDefault("placeholder", ""));
                case "toggle" -> builder.toggle(label);
                case "slider" -> {
                    List<Float> values = (List<Float>) data.get("stepper");
                    if (values.size() >= 4) {
                        builder.slider(label, values.get(0), values.get(1), values.get(2), values.get(3));
                    }
                }
                default -> dataManager.sendMessage(console, mm.deserialize("<red>[" + form + "<red>] Error in sections"));
            }
        }

        Audience a = plugin.adventure().player(player);
        handleCFormAction(player, builder, form);
        builder.invalidResultHandler(() -> dataManager.sendMessage(a, mm.deserialize("<red>Error Occured")));
        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());

    }

    private static void CreateSimpleForms(Player player, String form) {

        var mm = MiniMessage.miniMessage();

        Map<String, Object> formData = SformCache.get(form);
        if (formData == null) return;

        SimpleForm.Builder builder = SimpleForm.builder().title(form);
        builder.content((String) formData.get("content"));

        HashMap<Integer, String> actions = new HashMap<>();
        List<Map<String, String>> buttons = (List<Map<String, String>>) formData.get("buttons");

        int index = 0;
        for (Map<String, String> buttonData : buttons) {
            String label = buttonData.get("label");
            String action = buttonData.get("action");

            if (buttonData.containsKey("img-type")) {
                FormImage.Type type = FormImage.Type.valueOf(buttonData.get("img-type"));
                String source = buttonData.get("source");
                builder.button(label, type, source);
            } else {
                builder.button(label);
            }

            actions.put(index, action);
            index++;
        }

        Audience a = plugin.adventure().player(player);

        builder.validResultHandler(response -> {
            Bukkit.dispatchCommand(player, actions.get(response.clickedButtonId()));
        });

        builder.invalidResultHandler(() -> dataManager.sendMessage(a, mm.deserialize("<red>Error Occured")));

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());

    }

    private static void handleCFormAction(Player player, CustomForm.Builder builder, String form) {
        String actionType = (String) CformCache.get(form).get("action").get("type");

        if (actionType.equalsIgnoreCase("command")) {
            String command = (String) CformCache.get(form).get("action").get("command");

            builder.validResultHandler(response -> {
                String replacedCommand = command;
                int index = 0;
                for (String actions : CformCache.get(form).keySet()) {
                    if (actions.equalsIgnoreCase("action")) {
                        continue;
                    }
                    String resp = "";
                    if (CformCache.get(form).get(actions).get("type").equals("dropdown")) {
                        List<String> options = (List<String>) CformCache.get(form).get(actions).get("options");
                        if (options.contains("players")) {
                            options.remove("players");
                            Bukkit.getOnlinePlayers().forEach(oplayer -> {
                                String name = oplayer.getName();
                                if (!(name.equals(player.getName()))) {
                                    options.add(name);
                                }
                            });
                        }
                        resp = options.get(Integer.parseInt(Objects.requireNonNull(response.valueAt(index)).toString()));
                    } else {
                        resp = Objects.requireNonNull(response.valueAt(index)).toString();
                    }

                    replacedCommand = replacedCommand.replace("response" + index, Objects.requireNonNullElse(resp, "null"));
                    index++;
                }

                Bukkit.dispatchCommand(player, replacedCommand);
            });
        }
    }

    public static void OpenForm(Player player, String form, String formtype) {
        var mm = MiniMessage.miniMessage();
        Audience a = plugin.adventure().player(player);
        switch (formtype.toLowerCase()) {
            case "simple" -> {

                if (!SformCache.containsKey(form)) return;
                CreateSimpleForms(player, form);

            }

            case "custom" -> {

                if (!CformCache.containsKey(form)) return;
                CreateCustomForms(player, form);

            }

            default -> dataManager.sendMessage(a, mm.deserialize("<red>There's no such form"));
        }
    }



}
