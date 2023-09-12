package Main;

import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.input.Mouse;
import java.util.Random;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.friend.Friend;
import org.dreambot.api.methods.friend.Friends;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;



@ScriptManifest(
        name = "Bredz's PM spam",
        author = "Bredz",
        version = 1.2D,
        category = Category.UTILITY
)

public class Main extends AbstractScript {
    String PlayerName = "";
    public String preset_message = "";
    int state;
    int seen_counter;
    int typing_speed;
    public boolean isStarted;
    public boolean set_type_speed;
    public boolean hop;
    public boolean member;
    public boolean timerCatch;

    int[] f2p_worlds = {301, 308, 316, 380, 379, 326, 383};
    int[] p2p_worlds = {330, 302, 465, 344, 321, 305, 492, 329, 477};


    List<Integer> worlds_we_been_in = new ArrayList<>();
    List<String> seen_players = new ArrayList<>();
    List<Friend> allFriendsArray = new ArrayList<>();
    List<PlayerClass> playerObjectsArray = new ArrayList<>();
    List<String> messaged_players_list = new ArrayList<>();
    Timer hopTimer = new Timer(60000);
    Tile start_tile;
    boolean set_tile = true;

    @Override
    public void onStart() {
        set_type_speed = true;
        this.worlds_we_been_in.add(Worlds.getCurrentWorld());
        Keyboard.setWordsPerMinute(100);
        if (PlayerName.equals("")) {
            PlayerName = getLocalPlayerName();
        }
        this.state = 0;
        updatePlayerObjectsArray();
        SwingUtilities.invokeLater(() -> {
            new Gui(this);
        });
        timerCatch = true;
    }

    @Override
    public int onLoop() {
        if (isStarted){
            state_handler();
            if (set_type_speed){
                Keyboard.setWordsPerMinute(typing_speed);
                set_type_speed = false;
            }
        }
        if (set_tile){
            start_tile = getLocalPlayer().getTile();
            set_tile = false;
        }
        return Calculations.random(500, 600);
    }

    private void reset_scroll_bar() {
        WidgetChild up_arrow = Widgets.getWidgetChild(429, 12, 4);
        Rectangle rect = new Rectangle(up_arrow.getRectangle().x, up_arrow.getRectangle().y,
                up_arrow.getRectangle().width - 3, up_arrow.getRectangle().height - 3);
        Random rand = new Random();
        for (int i = 0; i < rand.nextInt(10) + 50; i++) {
            Mouse.move(rect);
            Mouse.click(false);
        }
    }

    private String getLocalPlayerName() {
        Player LocalPlayer = getLocalPlayer();
        return LocalPlayer.getName();
    }

    public void update_friends_array() {
        if (this.allFriendsArray.size() > 1) {
            this.allFriendsArray.clear();
        }
        this.allFriendsArray.addAll(Arrays.asList(Friends.getFriends()));
    }

    public boolean checkNameInPlayerObjectsArrayString(String name_to_check) {
        for (PlayerClass playerClass : this.playerObjectsArray) {
            if (playerClass.player_name.equals(name_to_check)) {
                return true;
            }
        }
        return false;
    }
    public void updatePlayerObjectsArray() {
        for (Friend friend : this.allFriendsArray) {
            if (!checkNameInPlayerObjectsArrayString(friend.getName())) {
                PlayerClass player_to_add = new PlayerClass();
                player_to_add.setValues(friend.getName(), false);
                playerObjectsArray.add(player_to_add);
            }
        }
    }

    public boolean isFriendListPopulated() {
        update_friends_array();
        return this.allFriendsArray.size() > 100;
    }

    public void state_handler() {
        update_friends_array();
        if (!isFriendListPopulated() && state < 1) {
            state = 0; // add players
        }
        if (isFriendListPopulated() && state != 2) {
            state = 1; // send messages
        }
        if (start_tile != null) {
            if (!getLocalPlayer().getTile().equals(start_tile)){
                Walking.walk(start_tile);
            }
        }

        switch (this.state) {
            case (0):
                add_players();
                break;
            case (1):
                send_messages();
                break;
            case (2):
                delete_friends();
                break;
        }
    }
    public void hop_worlds(){
        if (member){
            WorldHopper.hopWorld(GetRandomMembersWorld());
        } else {
            WorldHopper.hopWorld(GetRandomNonMembersWorld());
        }
        sleep(12000, 13000);
    }

    public World GetRandomNonMembersWorld() {
        for (int f2p_world : f2p_worlds) {
            if (!worlds_we_been_in.contains(f2p_world)) {
                return Worlds.getWorld(f2p_world);
            }
        }
        return Worlds.getRandomWorld((w) -> w.isF2P() && w.isNormal()
                && Skills.getTotalLevel() >= w.getMinimumLevel());
    }

    public World GetRandomMembersWorld() {
        for (int p2p_world : p2p_worlds) {
            if (!worlds_we_been_in.contains(p2p_world)) {
                return Worlds.getWorld(p2p_world);
            }
        }
        return Worlds.getRandomWorld((w) -> w.isMembers() && w.isNormal() &&
                Skills.getTotalLevel() >= w.getMinimumLevel());
    }
    public void delete_friends() {
        Tabs.openWithMouse(Tab.FRIENDS);
        List<Friend> get_friends = Arrays.asList(Friends.getFriends());
        if ((long) get_friends.size() < 1) {
            this.state = 0;
        } else {
            for (Friend get_friend : get_friends) {
                Friends.deleteFriend(get_friend);
                sleep(300, 400);
            }
        }
    }

    public boolean check_elapsed() {
        if (timerCatch) {
            timerCatch = false;
            hopTimer.reset();
            return false;
        }
        return hopTimer.finished();
    }

    public void add_players() {
        if (isFriendListPopulated()) {
            state = 1;
        }
        if (Dialogues.canEnterInput())
            Keyboard.type("", true);

        List<Player> playerNames = Players.all(p -> Objects.nonNull(p) && p.distance(getLocalPlayer()) <= 50);
        if (state == 1){
            log("Send Messages to online");
        } else {
            for (Player playerName : playerNames) {
                String player = playerName.getName();
                update_friends_array();
                if (isFriendListPopulated()) {
                    state = 1;
                    timerCatch = true;
                }
                if (state != 1){
                    if (!Friends.haveFriend(player) && !player.equals(PlayerName) && !seen_players.contains(player)) {
                        Tabs.openWithMouse(Tab.FRIENDS);
                        Friends.addFriend(player);
                        seen_players.add(player);
                        hopTimer.reset();
                    }
                }
                if (hop){
                    if (check_elapsed()){
                        hop_worlds();
                        timerCatch = true;
                    }
                }
            }
        }
    }

    private boolean have_messaged(String name){
        if (this.seen_counter > 10){
            state = 2;
            this.seen_counter = 0;
        }
        for (String s : this.messaged_players_list) {
            if (this.messaged_players_list.contains(name)) {
                this.seen_counter++;
                return true;
            } else {return false;
            }
        }
        return false;
    }
    private void interact_and_type_message(WidgetChild wc){
        if (!have_messaged(wc.getText())){
            if (!(wc.getX() < 240)){
                wc.interact();
                sleepUntil(Dialogues::canEnterInput, 1000);
                if (Dialogues.canEnterInput()){
                    Keyboard.type(preset_message, true);
                    this.messaged_players_list.add(wc.getText());
                    sleep(2000,2200);
                }
            }
        }
    }

    private void send_messages(){
        if (this.state != 2) {
            if (!Tabs.isOpen(Tab.FRIENDS)) {
                Tabs.openWithMouse(Tab.FRIENDS);
            }
            reset_scroll_bar();

            update_friends_array();
            updatePlayerObjectsArray();

            WidgetChild friendsWidget = Widgets.getWidgetChild(429, 11);
            Arrays.asList(Friends.getFriends()).forEach(f -> {
                for (WidgetChild wc : friendsWidget.getChildren()) {
                    if (wc != null) {
                        if (wc.getText().equals(f.getName())) {
                            if (f.isOnline()) {
                                if (!(wc.getX() < 240)){
                                    if (wc.getRelativeY() >= 165) {
                                        if (wc.getRelativeY() == 165) {
                                            move_widget_location_down();
                                        } else if (wc.getRelativeY() == 195) {
                                            move_widget_location_down();
                                        } else if (wc.getRelativeY() == 225) {
                                            move_widget_location_down();
                                        } else if (wc.getRelativeY() == 255) {
                                            move_widget_location_down();
                                        } else if (wc.getRelativeY() == 315) {
                                            move_widget_location_down();
                                        } else if (wc.getRelativeY() == 345) {
                                            move_widget_location_down();
                                        } else if (wc.getRelativeY() == 375) {
                                            move_widget_location_down();
                                        } else if (wc.getRelativeY() == 405) {
                                            move_widget_location_down();
                                        }
                                    }
                                    interact_and_type_message(wc);
                                }
                            }
                        }
                    }
                }
            });
            this.state = 2;
        }
    }
    public void move_widget_location_down() {
        WidgetChild down_arrow = Widgets.getWidgetChild(429, 12, 5);
        Rectangle rect = new Rectangle(down_arrow.getRectangle().x, down_arrow.getRectangle().y,
                down_arrow.getRectangle().width-3, down_arrow.getRectangle().height-3);

        Mouse.move(rect);
        Mouse.click(false);
        Mouse.click(false);
    }
}
