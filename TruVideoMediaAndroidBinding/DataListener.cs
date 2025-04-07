namespace TruVideoMediaAndroidBinding;

public class DataListener : Java.Lang.Object, TruVideoMediaAndroid.IDataListener
{
    // Event to forward data to .NET MAUI
    public event Action<string>? DataReceived;

    public void OnDataReceived(string data)
    {
        DataReceived?.Invoke(data);
    }
}