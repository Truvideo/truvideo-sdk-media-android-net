namespace TruVideoMediaAndroidBinding;

public class MediaCallback : Java.Lang.Object, TruVideoMediaAndroid.IMediaCallback
{
    private readonly Action<string> _onSuccess;
    private readonly Action<string> _onFailure;

    public MediaCallback(Action<string> onSuccess, Action<string> onFailure) {
        _onSuccess = onSuccess;
        _onFailure = onFailure;
    }

    public void OnSuccess(string result) {
        _onSuccess?.Invoke(result);
    }

    public void OnFailure(string error) {
        _onFailure?.Invoke(error);
    }
}