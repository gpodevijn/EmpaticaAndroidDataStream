package be.ac.ulb.iridia.empaticadatastream;

public class PhysioData {
    private Float mValue;
    private String mType;
    private String mTimestamp;
    private boolean mIsTagged;

    public PhysioData(String type, Float value, String timestamp, boolean isTagged) {
        mType = type;
        mValue = value;
        mTimestamp = timestamp;
        mIsTagged = isTagged;
    }

    public void setTagged() {
        mIsTagged = true;
    }

    public String getValue() {
        return Float.toString(mValue);
    }

    public String getTimestamp() {
        return mTimestamp;
    }

    public boolean isTagged() {
        return mIsTagged;
    }
}
