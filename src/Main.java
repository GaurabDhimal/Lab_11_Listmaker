import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import static java.nio.file.StandardOpenOption.CREATE;

public class Main
{
    private static Scanner in = new Scanner(System.in);
    private static ArrayList<String> lines = new ArrayList<>();
    private static boolean dirty = false;

    private static File currFile;
    private static String currFileName;

    public static void main(String[] args)
    {
        String menuPrompt = "O - Open\tS - Save\tA - Add\tD - Delete\tV - View\tQ - Quit";
        String choice = "";

        boolean done = false;

        try
        {
            do
            {
                showList();
                choice = SafeInput.getRegExString(in, menuPrompt, "[OoSsaAaDdVvQq]");
                choice = choice.toUpperCase();

                switch(choice)
                {
                    case "O":
                        if(dirty)
                        {
                            System.out.println("You are going to lose the current data!");
                            boolean saveYN = SafeInput.getYNConfirm(in, "Do you want to save the current file?");

                            if(saveYN)
                                saveFile(currFileName);
                        }
                        openFile();
                        dirty = false;
                        break;
                    case "S":
                        if(!lines.isEmpty())
                        {
                            if(currFile == null)
                                currFileName = SafeInput.getNonZeroLenString(in, "Enter");
                            else
                                currFileName = currFile.getName();

                            saveFile(currFileName);
                            dirty = false;
                        }
                        else
                            System.out.println("There's nothing to save!");
                        break;
                    case "A":
                        add();
                        dirty = true;
                        break;
                    case "D":
                        delete();
                        dirty = true;
                        break;
                    case "V":
                        showList();
                        break;
                    case "Q":
                        quit();
                        break;
                }

            }while(!done);
        }
        catch(FileNotFoundException e)
        {
            System.out.println("File not found!");
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void showList()
    {
        System.out.println("----------------------------------------------");

        if(lines.size() == 0)
            System.out.println("\nThe list is currently empty.\n");
        else
        {
            for(String l : lines)
                System.out.println("\t" + l);
        }
        System.out.println("----------------------------------------------");
    }

    private static void quit() throws IOException
    {
        boolean quitYN = false;
        boolean saveYN = false;

        if(dirty)
        {
            System.out.println("You are going to lose the current data!");
            System.out.println("Save the file before quitting?");

            saveYN = SafeInput.getYNConfirm(in, "Save the file? ");
            if(saveYN)
            {
                saveFile(currFileName);
                System.out.println("File saved, exiting...");
            }
            System.exit(0);
        }

        quitYN = SafeInput.getYNConfirm(in, "Are you sure you want to quit? ");
        if(quitYN)
            System.exit(0);

    }

    private static void add()
    {
        String lineItem = "";
        lineItem = SafeInput.getNonZeroLenString(in, "Enter the new line");
        lines.add(lineItem);
    }

    private static void delete()
    {
        System.out.println("----------------------------------------------");
        if(lines.size() == 0)
        {
            System.out.println("\nThere is nothing to delete!\n");
            System.out.println("----------------------------------------------");
            return;
        }
        else
        {
            int line = 0;

            for(String l : lines)
            {
                line++;
                System.out.printf("\t%3d  %-80s\n", line, l);
            }
        }
        System.out.println("----------------------------------------------");
        int low = 1;
        int high = lines.size();
        int target = SafeInput.getRangedInt(in, "Enter the number of the line you wish to delete", 1, lines.size());
        target--; // index starts at 0

        lines.remove(target);
        return;
    }

    private static void openFile() throws IOException, FileNotFoundException
    {
        JFileChooser chooser = new JFileChooser();
        File selectedFile;
        String rec = "";

        File workingDirectory = new File(System.getProperty("user.dir"));
        Path wd = workingDirectory.toPath();
        wd = Paths.get(workingDirectory + "\\src");
        workingDirectory = wd.toFile();

        chooser.setCurrentDirectory(workingDirectory);

        if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
        {
            selectedFile = chooser.getSelectedFile();
            currFile = selectedFile;
            Path File = selectedFile.toPath();

            InputStream in =
                    new BufferedInputStream(Files.newInputStream(File, CREATE));
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(in));

            lines.clear();

            int ln = 0;
            while(reader.ready())
            {
                rec = reader.readLine();
                ln++;
                lines.add(rec);
                System.out.printf("\nLine %4d %-60s ", ln, rec);
            }
            reader.close();
            System.out.println("\n\nData file read!");
        }
        else
            System.out.println("No file selected! ... exiting.\nTry again");
    }

    private static void saveFile(String fileName) throws IOException
    {
        File workingDirectory = new File(System.getProperty("user.dir"));
        Path file = Paths.get(workingDirectory.getPath() + "\\src\\" + fileName);

        OutputStream out =
                new BufferedOutputStream(Files.newOutputStream(file, CREATE));
        BufferedWriter writer =
                new BufferedWriter(new OutputStreamWriter(out));

        if(currFile == null)
            currFile = file.toFile();

        for(String rec : lines)
        {
            writer.write(rec, 0, rec.length());
            writer.newLine();
            System.out.println("Writing file data: " + rec);
        }
        writer.close();
        System.out.println("Data file written: " + currFile.getName());
    }
}