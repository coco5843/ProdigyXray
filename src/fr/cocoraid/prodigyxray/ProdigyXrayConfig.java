package fr.cocoraid.prodigyxray;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cocoraid on 20/06/2017.
 */
public class ProdigyXrayConfig extends Config {

    static String comment[] = {"/*Welcome to ProdigyXray  configuration*/", " ", " ", " "};


    @ConfigOptions(name = "message.permission")
    public String permission = "&4You do not have the permission to do this";
    @ConfigOptions(name = "message.disabled")
    public String disabled = "&4We're sorry, this ore is disabled at the moment..";
    @ConfigOptions(name = "message.money")
    public String money = "&4You do not have enough money, you need %amount $";
    @ConfigOptions(name = "message.withdrawmoney")
    public String withdrawmoney = "&2Xray mode activated for %amount £";
    @ConfigOptions(name = "message.cooldown")
    public String cooldownmsg = "&4You must wait %time second(s) to use the xray mode";
    @ConfigOptions(name = "message.alreadyUsing")
    public String alreadyusing = "&4You're already using the xray mode";
    @ConfigOptions(name = "message.available")
    public String available = "&cAvailable ores: ";
    @ConfigOptions(name = "xray.duration")
    public int duration = 10;
    @ConfigOptions(name = "xray.blockDistance")
    public int distance = 20;
    @ConfigOptions(name = "xray.cooldown")
    public int cooldown = 20;
    @ConfigOptions(name = "xray.cost")
    public int cost = 100;
    @ConfigOptions(name = "xray.buyable")
    public boolean buyable = false;
    @ConfigOptions(name = "xray.disabledOres")
    public List<String> disabledOre = new ArrayList<>(Arrays.asList("emerald","lapis"));

    public ProdigyXrayConfig(final File file) {
        super(file, Arrays.asList(comment));
    }


}
