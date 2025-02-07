package me.isra.hgkits.managers;

public class FameManager {

    public static String getFameRank(int fame) {
        if (fame >= 300000) return "Heroe Mitico";
        else if (fame >= 150000) return "1 Heroe Legendario";
        else if (fame >= 125000) return "2 Heroe Emperador";
        else if (fame >= 95000) return "3 Rey Heroe";
        else if (fame >= 75000) return "4 Heroe Eminente";
        else if (fame >= 45000) return "5 Heroe Ilustre";
        else if (fame >= 12000) return "6 Heroe Renombrado";
        else if (fame >= 7050) return "7 Heroe Conquistador";
        else if (fame >= 3650) return "8 Heroe Terrorifico";
        else if (fame >= 1750) return "9 Heroe Mortal";
        else if (fame >= 675) return "10 Heroe Poderoso";
        else if (fame >= 250) return "11 Heroe Feroz";
        else if (fame >= 75) return "12 Heroe";
        else if (fame >= 25) return "Aprendiz";
        else return "Nuevo";
    }

    public static String getRankByFame(Object fame) {
        if ((int) fame >= 300000) return "Mítico";
        if ((int) fame >= 150000) return "Legendario";
        if ((int) fame >= 125000) return "Emperador";
        if ((int) fame >= 95000) return "Rey";
        if ((int) fame >= 75000) return "Eminente";
        if ((int) fame >= 45000) return "Ilustre";
        if ((int) fame >= 12000) return "Renombrado";
        if ((int) fame >= 7050) return "Conquistador";
        if ((int) fame >= 3650) return "Terrorífico";
        if ((int) fame >= 1750) return "Mortal";
        if ((int) fame >= 675) return "Poderoso";
        if ((int) fame >= 250) return "Feroz";
        if ((int) fame >= 75) return "Héroe";
        if ((int) fame >= 25) return "Aprendiz";
        return "Nuevo";
    }

    public static String getRankColor(int fame) {
        if (fame >= 300000) return "&0";
        else if (fame >= 150000) return "&8";
        else if (fame >= 125000) return "&4";
        else if (fame >= 95000) return "&c";
        else if (fame >= 75000) return "&6";
        else if (fame >= 45000) return "&e";
        else if (fame >= 12000) return "&d";
        else if (fame >= 7050) return "&5";
        else if (fame >= 3650) return "&1";
        else if (fame >= 1750) return "&9";
        else if (fame >= 675) return "&b";
        else if (fame >= 250) return "&3";
        else if (fame >= 75) return "&2";
        else if (fame >= 25) return "&a";
        else return "&7";
    }
}
