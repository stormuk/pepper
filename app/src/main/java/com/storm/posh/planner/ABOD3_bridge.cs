using System.IO;
using System.Net.Sockets;

public class ABOD3_Bridge
{
    private static ABOD3_Bridge instance;

    public static ABOD3_Bridge GetInstance()
    {

        if (instance == null)
        {
            instance = new ABOD3_Bridge();
        }

        return instance;
    }

    private StreamWriter streamWriter;

    private int selectedBotNumber = 1;

    private ABOD3_Bridge()
    {
        Init();
    }

    private void Init()
    {
        try
        {
            TcpClient client = new TcpClient("localhost", 3000);

            Stream stream = client.GetStream();
            streamWriter = new StreamWriter(stream);
            streamWriter.AutoFlush = true;
        }
        catch
        {

        }
    }

    internal void ChangeSelectedBot(int newSlectedBot)
    {
        this.selectedBotNumber = newSlectedBot;
    }

    internal void AletForElement(int botNumber, string elementName, string elementType)
    {
        if (streamWriter != null && botNumber == selectedBotNumber)
        {
            streamWriter.WriteLine(botNumber + "," + elementName + "," + elementType);
        }
    }

    internal void AlertForSense(string SenseName, int botNumber)
    {
        if (streamWriter != null && botNumber == selectedBotNumber)
        {
            streamWriter.WriteLine(SenseName);
        }
    }

    internal void AlertForGoal(string goalName, int botNumber)
    {
        if (streamWriter != null && botNumber == selectedBotNumber)
        {
            streamWriter.WriteLine(goalName);
        }
    }
}