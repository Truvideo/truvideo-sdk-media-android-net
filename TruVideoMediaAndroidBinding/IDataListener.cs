using Android.Runtime;
using Java.Interop;

namespace TruVideoMediaAndroidBinding;

public interface IDataListener : IJavaObject, IDisposable
{
    void OnDataReceived(string data);
}