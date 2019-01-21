package com.csr460.iskipper_android.device;

import java.util.ArrayDeque;
import java.util.Queue;

import com.csr460.iSkipper.device.AbstractSerialAdapter;
import com.csr460.iSkipper.device.ReceivedPacketEvent;
import com.csr460.iSkipper.emulator.SerialSymbols;
import com.csr460.iSkipper.handler.PrintHandler;
import com.csr460.iSkipper.handler.ReceivedPacketHandlerInterface;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

public class SerialAdapter extends AbstractSerialAdapter {

    private static final int RECEIVE_BUFFER_SIZE = 1024;
    private static final int RECEIVE_PACKET_QUEUE_SIZE = 8;
    private final static int OPEN_SERIAL_WAIT_TIME = 1500;
    private CH34xUARTDriver driver;
    private Queue<ReceivedPacketEvent> unhandledPackets;
    private Thread serialReaderThread;
    private Thread packetEventSenderThread;
    private boolean isReady;

    public SerialAdapter(CH34xUARTDriver driver) {
        if (driver == null)
            throw new NullPointerException("CH34xUARTDriver cannot be null when initializing SerialAdapter");
        this.driver = driver;
        this.packetHandler = new PrintHandler();
        this.unhandledPackets = new ArrayDeque<>(RECEIVE_PACKET_QUEUE_SIZE);
    }

    @Override
    public void setPacketHandler(ReceivedPacketHandlerInterface packetHandler) {
        super.setPacketHandler(packetHandler);
    }

    @Override
    public ReceivedPacketHandlerInterface getPacketHandler() {
        return super.getPacketHandler();
    }

    @Override
    public boolean isAvailable() {
        return driver.isConnected();
    }

    @Override
    public boolean close() {
        driver.CloseDevice();
        return true;
    }

    @Override
    public void writeBytes(byte[] bytes) {
        driver.WriteData(bytes, bytes.length);
    }

    @Override
    public void writeByte(byte b) {
        driver.WriteData(new byte[]{b}, 1);
    }

    public boolean usbFeatureSupported() {
        return driver.UsbFeatureSupported();
    }

    public boolean openDevice() {
        driver.CloseDevice();
        return driver.ResumeUsbList() == 0 && driver.UartInit();
    }

    public boolean configPorts() {
        if (driver.SetConfig(115200/*BaudRate*/, (byte) 8/*DataBits*/, (byte) 1/*StopBits*/, (byte) 0/*Parity*/, (byte) 0/*CTS/RTS*/)) {
            driver.WriteData(new byte[]{SerialSymbols.OP_RESET},1);
            isReady = true;
            initPacketEventSenderThread();
            initSerialReaderThread();
            try {
                Thread.sleep(OPEN_SERIAL_WAIT_TIME);
            }catch (InterruptedException e){
                e.printStackTrace();
            }

        }
        return isReady;
    }

    private void initSerialReaderThread() {
        if(serialReaderThread!=null)
            serialReaderThread.interrupt();
        serialReaderThread = new Thread(new Runnable() {
            private byte[] buffer = new byte[RECEIVE_BUFFER_SIZE];
            private int currentIndex = 0;
            private byte[] b = new byte[1];

            @Override
            public void run() {
                while (true) {
                    if (driver.ReadData(b, 1) >= 1) {//If we have received one byte
                        buffer[currentIndex++] = b[0];
                        if (b[0] == '\n' || b[0] == '\0') {//If this is the end of a completed packet
                            byte[] packet = new byte[currentIndex];
                            System.arraycopy(buffer, 0, packet, 0, currentIndex);
                            currentIndex = 0;
                            unhandledPackets.offer(new ReceivedPacketEvent(this, packet));
                            synchronized (packetEventSenderThread) {
                                packetEventSenderThread.notifyAll();
                            }
                        }
                    }
                }
            }
        });
        serialReaderThread.start();
    }

    private void initPacketEventSenderThread() {
        if (packetEventSenderThread != null)
            packetEventSenderThread.interrupt();
        packetEventSenderThread = new Thread(()->{
            while(true) {
                while(!unhandledPackets.isEmpty()){
                    getPacketHandler().onReceivedPacketEvent(unhandledPackets.poll());
                }
                synchronized (packetEventSenderThread) {
                    try{
                        packetEventSenderThread.wait();
                    }catch (InterruptedException e){
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        packetEventSenderThread.start();
    }

}
