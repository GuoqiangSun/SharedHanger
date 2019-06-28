package cn.com.startai.sharedhanger;

/**
 * author Guoqiang_Sun
 * date 2019/5/24
 * desc
 */
public interface IBleData {
    void sendData(byte[] buf);

    void receiveData(byte[] buf);
}
