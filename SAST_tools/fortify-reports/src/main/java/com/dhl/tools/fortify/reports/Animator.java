package com.dhl.tools.fortify.reports;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Animator {
    private static final List<String> ANIMATION_FRAMES = Arrays.asList("|", "/", "-", "\\");
    private AtomicBoolean done = new AtomicBoolean(false);
    private Thread animationThread;
    private static Animator animator = new Animator();

    private void animate() {
        int frameIndex = 0;
        System.out.print("\rProcessing  ");
        while (!done.get()) {
            String frame = ANIMATION_FRAMES.get(frameIndex);
            System.out.print("\b" + frame);
            System.out.flush();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            frameIndex = (frameIndex + 1) % ANIMATION_FRAMES.size();
        }
        System.out.println("\rCompleted!     ");
    }

    public static void start() {
    	stop();
    	animator.done.set(false);
    	animator.animationThread = new Thread(animator::animate);
    	animator.animationThread.start();
    }
    public static void stop() {
    	animator.done.set(true);
        try {
            if (animator.animationThread != null)
            	animator.animationThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
    	Animator.start();
    	Thread.sleep(5000);
    	Animator.stop();
	}
}