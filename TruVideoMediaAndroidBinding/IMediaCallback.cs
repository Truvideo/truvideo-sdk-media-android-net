using Android.Runtime;
using Java.Interop;

namespace TruVideoMediaAndroidBinding;

[Register("com.truvideo.media.MediaCallback")]
public interface IMediaCallback : IJavaObject, IDisposable
{
    [Export("onSuccess")]
    void OnSuccess(string result);

    [Export("onFailure")]
    void OnFailure(string error);
}