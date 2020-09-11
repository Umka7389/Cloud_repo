public class FileMessage extends AbstractMessage {
    private String name;
    private byte[] data;

    public FileMessage() {
    }

    private FileMessage(FileMessageBuilder builder) {
        name = builder.nameB;
        data = builder.dataB;
    }

    public static FileMessageBuilder builder() {
        return new FileMessageBuilder();
    }

    public static class FileMessageBuilder {

        private String nameB;
        private byte[] dataB;

        public FileMessageBuilder name(String name) {
            nameB = name;
            return this;
        }

        public FileMessageBuilder data (byte[] data) {
            dataB = data;
            return this;
        }

        public FileMessage build() {
            return new FileMessage(this);
        }
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }
}
