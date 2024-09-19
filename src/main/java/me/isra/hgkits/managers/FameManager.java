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
        if ((int) fame >= 200000) return "Héroe Mítico";
        if ((int)fame >= 100000) return "Héroe Legendario";
        if ((int)fame >= 50000) return "Héroe Emperador";
        if ((int)fame >= 30000) return "Rey Héroe";
        if ((int)fame >= 20000) return "Héroe Eminente";
        if ((int)fame >= 15000) return "Héroe Ilustre";
        if ((int)fame >= 10000) return "Héroe Renombrado";
        if ((int)fame >= 5000) return "Héroe Conquistador";
        if ((int)fame >= 2000) return "Héroe Terrorífico";
        if ((int)fame >= 1000) return "Héroe Mortal";
        if ((int)fame >= 500) return "Héroe Poderoso";
        if ((int)fame >= 200) return "Héroe Feroz";
        if ((int)fame >= 100) return "Héroe";
        if ((int)fame >= 50) return "Aprendiz";
        return "Nuevo";
    }
}
