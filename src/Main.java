import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static int key;
    //заранее ортированный массив алфавита
    static final char[] arrayRU = {' ', '!', '"', ',', '-', '.', ':', '?',
            'Ё', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ж', 'З',
            'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т',
            'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я',
            'а', 'б', 'в', 'г', 'д', 'е', 'ж', 'з',
            'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т',
            'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', 'ё'};

    public static void main(String[] args) {
        String pathIN = getPathIn();
        boolean crypt = getCryptMode();
        key = getCryptKey(crypt);
        String pathOUT = getNewFileName(pathIN, crypt);

        try (FileReader reader = new FileReader(pathIN);
             FileWriter writer = new FileWriter(pathOUT)) {

            char[] buff = new char[2048];

            while (reader.ready()) {
                int real = reader.read(buff);
                if (crypt) {
                    cryptBuff(buff);
                } else {
                    buff = decryptBuff(buff);
                }

                writer.write(buff, 0, real);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //расшифровка буфера
    private static char[] decryptBuff(char[] array) {
        if (key != -1) {
            for (int i = 0; i < array.length; i++) {
                int index = getIndex(array[i]);
                if (index >= 0) {
                    if (index - key < 0) {
                        index = arrayRU.length + index - key;
                    } else index = index - key;
                    array[i] = arrayRU[index];
                }
            }
            return array;
        } else {
            key =1;
            while (true) {
                char[] tempBuf = array.clone();
                decryptBuff(tempBuf);
                boolean firstRight = false;
                for (int i = 0; i < 100; i++) {
                    if ((tempBuf[i] == ',' && tempBuf[i + 1] != ' ') || (tempBuf[i] == '.' && tempBuf[i + 1] != ' ')) {
                        break;
                    }
                    if ((tempBuf[i] == ',' && tempBuf[i + 1] == ' ') || (tempBuf[i] == '.' && tempBuf[i + 1] == ' ')) {
                        if ( firstRight) {
                            return tempBuf;
                        } else firstRight = true;

                    }
                }
                key++;
            }
        }

    }

    //шифрование буфера
    private static void cryptBuff(char[] array) {
        for (int i = 0; i < array.length; i++) {
            int index = getIndex(array[i]); //берем индекс символа по нашему алфавиту
            if (index > -1) {
                if ((index + key) > arrayRU.length - 1) {
                    index = ((index + key) - arrayRU.length);
                } else index = index + key;
                array[i] = arrayRU[index];
            }
        }
    }

    //получаем индекс символа из буфера
    private static int getIndex(char ch) {
        return Arrays.binarySearch(arrayRU, ch);
    }

    //получаем новое имя файла для записи резльтата
    private static String getNewFileName(String oldFileName, boolean crypt) {
        int dotIndex = oldFileName.lastIndexOf(".");
        if (crypt) {
            return oldFileName.substring(0, dotIndex) + "-crypt" + oldFileName.substring(dotIndex);
        } else return oldFileName.substring(0, dotIndex) + "-decrypt" + oldFileName.substring(dotIndex);

    }

    //получаем ключ шифрования
    private static int getCryptKey(boolean crypt) {
        String error = "Введено не верное значение!";
        if (crypt) {
            while (true) {
                System.out.println("Введите номер ключа шифрования от 1 до 40");
                if (scanner.hasNextInt()) {
                    int result = scanner.nextInt();
                    if (result <= 40 && result >= 1) {
                        return result;
                    } else System.out.println(error);
                }
            }
        } else {
            while (true) {
                System.out.println("Введите номер ключа шифрования от 1 до 40 или слово \"авто\"," +
                        "\nесли хотите использовать режим автоподбора brute force");
                if (scanner.hasNextInt()) {
                    int result = scanner.nextInt();
                    if (result <= 40 && result >= 1) {
                        return result;
                    } else System.out.println(error);
                    System.out.println(error);
                } else if (scanner.hasNextLine()) {
                    String value = scanner.nextLine();
                    if (value.equalsIgnoreCase("авто")) {
                        return -1;
                    } else System.out.println(error);
                }
            }
        }
    }

    //выбор режима шифрования
    private static boolean getCryptMode() {
        System.out.println("Для шифрования введите \"Да\" \nДля расшифровки введите \"Нет\"");
        String error = "Введено не верное значение. Ведите Да или Нет";
        String answer;
        while (true) {
            if (scanner.hasNextLine()) {
                answer = scanner.nextLine();
            } else continue;

            if (answer.equalsIgnoreCase("да")) {
                System.out.println("Выбран режим шифрования!");
                return true;
            } else if (answer.equalsIgnoreCase("нет")) {
                System.out.println("Выбран режим расшифровки!");
                return false;
            } else {
                System.out.println(error);
            }
        }
    }

    //получаем путь исходного файла
    private static String getPathIn() {
        String path;
        String error = "Не верный путь к файлу";
        while (true) {
            System.out.println("Введите путь к файлу");
            if (scanner.hasNextLine()) {
                path = scanner.nextLine();
            } else {
                System.out.println(error);
                continue;
            }
            try {
                if (Files.exists(Path.of(path))) {
                    return path;
                } else {
                    System.out.println(error);
                }
            } catch (Exception e) {
                System.out.println(error);
                return getPathIn();
            }
        }
    }
}
