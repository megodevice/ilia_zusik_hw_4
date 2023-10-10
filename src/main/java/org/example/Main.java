package org.example;

import java.util.Random;

public class Main {
    public static int bossHealth = 2000; // здоровье босса
    public static int bossDamage = 50;  // урон от босса
    public static String bossDefence;  // уязвимость босса
    public static int medicHillPoint = 25;  // на сколько пунктов лечит медик
    public static int witcherRecoveryPoints = 200; // сколько даётся здоровья воскрешонному игроку
    public static boolean bossIsKnockedNow = false;  // босс оглушен на раунд
    public static int[] heroesHealth = {250, 260, 270, 200, 500, 200, 200, 300};
    public static int[] heroesDamage = {20, 15, 10, 0, 10, 15, 0, 20};
    public static String[] heroesAttackType = {"Physical", "Magical", "Kinetic", "Medic", "Gollum", "Lucky", "Witcher", "Thor"};
    public static boolean canAttack[] = {true, true, true, false, true, true, false, true};

    // ------------------------------- каждого особого игрока может быть не более отдного ------------------------ //
    public static boolean canHill[] = {false, false, false, true, false, false, false, false};
    public static boolean canReduceDamage[] = {false, false, false, false, true, false, false, false};
    public static boolean canLuck[] = {false, false, false, false, false, true, false, false};
    public static boolean canRecover[] = {false, false, false, false, false, false, true, false};
    public static boolean canKnock[] = {false, false, false, false, false, false, false, true};

    public static int roundNumber = 0;


    public static void main(String[] args) {
        printStatistics();
        while (!isGameOver()) {
            playRound();
        }
    }

    public static void playRound() {
        roundNumber++;
        chooseBossDefence();
        bossAttack();
        System.out.println("----------------------------------------");
        System.out.println("The boss attacked the heroes");
        printStatistics();
        System.out.println("----------------------------------------");
        heroesAttack();
        // раунд окончен
        medicDoOwnWork(); // медик лечит
        witcherDoOwnWork(); // колдун воскрешает
        printStatistics();
    }

    public static void chooseBossDefence() {
        Random random = new Random();
        int randomIndex = random.nextInt(heroesAttackType.length); // 0,1,2
        while (!canAttack[randomIndex]) {                          // Выбрать уязвимость только тех игроков которые наносят удар
            randomIndex = random.nextInt(heroesAttackType.length);
        }
        bossDefence = heroesAttackType[randomIndex]; // если боссу повезет то у него будет уязвимость мертвого игрока
    }

    public static void bossAttack() { // Босс атакует
        Random random = new Random();
        boolean isLuckyNow = random.nextBoolean(); // Узнать повезло ли лаки в этом раунде
        int bossDamageForRound = bossDamage; // Урон от босса
        int gollumIndex = -1;
        for (int i = 0; i < canReduceDamage.length; i++) { // Найти голлема
            if (canReduceDamage[i]) {
                gollumIndex = i;
                break;
            }
        }
        if (gollumIndex != -1 && heroesHealth[gollumIndex] > 0) { // Если голлем найден и жив то атака босса снижена на 1/5
            bossDamageForRound -= bossDamage / 5;
        }
        if (!bossIsKnockedNow) {
            for (int i = 0; i < heroesHealth.length; i++) { // Атакуем игроков по очереди
                if (heroesHealth[i] > 0) { // Если игрок жив начинаем атаку
                    if (canLuck[i] && isLuckyNow) { // Если игрок может увернуться и это удалось то не присваивать урон и перейти к сделующему игроку
                        System.out.println("Lucky, got good luck!");
                        continue;
                    }
                    if (canReduceDamage[i]) { // Если атака идёт на голема то он принимает на себя полный удар
                        if (heroesHealth[i] - bossDamage <= 0) { // Если не может то погибает
                            heroesHealth[i] = 0;
                            bossDamageForRound = bossDamage; // и босс наносит остальным игрокам полный удар
                            continue;
                        } else {
                            heroesHealth[i] -= bossDamage; // либо принимает удар
                            continue;
                        }
                    } // Атака не на голема и не на лаки которому повезло
                    if (heroesHealth[i] - bossDamageForRound <= 0) { // Если босс убивает игрока завершаем атаку на текущего игрока
                        heroesHealth[i] = 0; // Если игрок погибает то голлем не принимает часть удара на себя
                    } else {
                        heroesHealth[i] -= bossDamageForRound; // иначе игрок получает удар
                        // Работа голлема
                        if (gollumIndex >= 0 && heroesHealth[gollumIndex] > 0) { // Если голлем жив, он найден
                            if (heroesHealth[gollumIndex] - (bossDamage / 5) <= 0) { // и если голлем не может больше брать на себя удар
                                heroesHealth[gollumIndex] = 0; // тогда голлем погибает
                                bossDamageForRound = bossDamage; // и босс наносит остальным игрокам полный удар
                            } else {
                                heroesHealth[gollumIndex] -= bossDamage / 5; // иначе берет на себя 1/5 удара
                            }
                        }
                    }
                }
            }
        } else {
            bossIsKnockedNow = !bossIsKnockedNow;
        }


    }

    public static void heroesAttack() {
        System.out.println("-------------------------------------------------");
        Random random = new Random();
        for (int i = 0; i < heroesDamage.length; i++) {
            if (heroesHealth[i] > 0 && bossHealth > 0) {
                int damage = heroesDamage[i];
                if (heroesAttackType[i] == bossDefence) {
                    int coeff = random.nextInt(9) + 2; // 2,3,4,5,6,7,8,9,10
                    damage = heroesDamage[i] * coeff;
                    System.out.println("Critical damage: " + damage + " by " + bossDefence);
                }
                if (bossHealth - damage < 0) {
                    bossHealth = 0;
                } else {
                    bossHealth -= damage; // bossHealth = bossHealth - damage;
                }
            }
        }

        int indexOfThor = -1;
        for (int i = 0; i < canKnock.length; i++) { // ищем тора
            if (canKnock[i]) {
                indexOfThor = i;
                break;
            }
        }
        if (indexOfThor != -1 && heroesHealth[indexOfThor] > 0) { // если найден и жив
            bossIsKnockedNow = random.nextBoolean(); // узнаём был ли нанесён оглушительный удар
            if (bossIsKnockedNow) {
                System.out.println("Boss was knocked!!!");
            }
        }
    }

    public static void printStatistics() {
        System.out.println("ROUND " + roundNumber + " ---------------");
        /*String defence;
        if (bossDefence == null) {
            defence = "No defence";
        } else {
            defence = bossDefence;
        }*/
        System.out.println("Boss health: " + bossHealth + " damage: " + bossDamage + " defence: " +
                (bossDefence == null ? "No defence" : bossDefence));
        for (int i = 0; i < heroesHealth.length; i++) {
            System.out.println(heroesAttackType[i] + " health: " + heroesHealth[i] + " damage: " + heroesDamage[i]);
        }
    }

    public static boolean isGameOver() {
        if (bossHealth <= 0) {
            System.out.println("Heroes won!!!");
            return true;
        }
        /*if (heroesHealth[0] <= 0 && heroesHealth[1] <= 0 && heroesHealth[2] <= 0) {
            System.out.println("Boss won!!!");
            return true;
        }
        return false;*/
        boolean allHeroesDead = true;
        for (int i = 0; i < heroesHealth.length; i++) {
            if (heroesHealth[i] > 0) {
                allHeroesDead = false;
                break;
            }
        }
        if (allHeroesDead) {
            System.out.println("Boss won!!!");
        }
        return allHeroesDead;
    }

    public static void medicDoOwnWork() {
        Random random = new Random();
        int indexOfMedic = -1;
        int whoGetHill = -1;
        for (int i = 0; i < canHill.length; i++) { // Поиск медика
            if (canHill[i]) {
                indexOfMedic = i;
                break;
            }
        }
        if (indexOfMedic >= 0 && heroesHealth[indexOfMedic] > 0) { // Если медик найден и жив
            for (int i = 0; i < canHill.length; i++) {
                if (heroesHealth[i] > 0 && heroesHealth[i] < 100 && !canHill[i]) { // Поиск живого игрока со здоровьем больше 0 и меньше 100 не являющегося медиком
                    whoGetHill = random.nextInt(heroesAttackType.length);
                    break;
                }
            }
            if (whoGetHill != -1) {
                while (heroesHealth[whoGetHill] <= 0 || heroesHealth[whoGetHill] > 100 || canHill[whoGetHill]) { // Выбираем живого игрока со здоровьем больше 0 и меньше 100 не являющегося медиком
                    whoGetHill = random.nextInt(heroesAttackType.length);
                }
                heroesHealth[whoGetHill] += medicHillPoint;
                System.out.println("The medic healed the " + heroesAttackType[whoGetHill] + " by " + medicHillPoint + " points");
            }
        }
    }

    public static void witcherDoOwnWork() {
        Random random = new Random();
        int indexOfWitcher = -1;
        int whoGetRecover = -1;
        for (int i = 0; i < canRecover.length; i++) { // Поиск колдуна
            if (canRecover[i]) {
                indexOfWitcher = i;
                break;
            }
        }
        if (indexOfWitcher != -1 && heroesHealth[indexOfWitcher] > 0) { // Если колдун найден и жив
            for (int i = 0; i < canRecover.length; i++) {
                if (heroesHealth[i] == 0) { // Поиск мёртвого игрока
                    whoGetRecover = random.nextInt(heroesAttackType.length);
                    break;
                }
            }
            if (whoGetRecover != -1) {
                while (heroesHealth[whoGetRecover] != 0) { // Выбор мёртвого игрока для воскрешения
                    whoGetRecover = random.nextInt(heroesAttackType.length);
                }
                heroesHealth[whoGetRecover] = witcherRecoveryPoints;  // игрок воскрешается
                heroesHealth[indexOfWitcher] = 0; // колдун погибает
                System.out.println("The witcher resurrected the player " + heroesAttackType[whoGetRecover] + " by " + witcherRecoveryPoints + " points");
            }
        }
    }
}
