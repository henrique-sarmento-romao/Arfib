import numpy as np
import matplotlib.pyplot as plt
from scipy.signal import find_peaks
import pandas as pd
import seaborn as sns
import random

# Pan-Tompkins R-R Interval Detection
def pan_tompkins(ecg_signal, sampling_rate):
    # Step 1: Bandpass Filter (0.5-450 Hz)
    from scipy.signal import butter, filtfilt
    
    # Butterworth bandpass filter
    def butter_bandpass(lowcut, highcut, fs, order=1):
        nyquist = 0.5 * fs
        low = lowcut / nyquist
        high = highcut / nyquist
        b, a = butter(order, [low, high], btype='band')
        return b, a

    lowcut = 0.5
    highcut = 45.0
    b, a = butter_bandpass(lowcut, highcut, sampling_rate, order=1)
    
    filtered_signal = filtfilt(b, a, ecg_signal)

    # Step 2: Differentiate the filtered signal
    differentiated_signal = np.diff(filtered_signal)

    # Step 3: Square the differentiated signal
    squared_signal = differentiated_signal ** 2

    # Step 4: Integrate the squared signal using Moving Average Filter (Window size: 150ms)
    window_size = int(sampling_rate * 0.150)  # 150 ms window for integration (0.150 seconds)
    
    # Moving average filter: Convolution with a window of size `window_size`
    integrated_signal = np.convolve(squared_signal, np.ones(window_size)/window_size, mode='same')

    # Step 5: Thresholding to find potential R-peaks
    threshold = np.max(integrated_signal) * 0.6  # Typically set as 60% of max value
    r_peaks, _ = find_peaks(integrated_signal, height=threshold, distance=int(sampling_rate * 0.4))  # Min distance between peaks

    # Step 6: Calculate R-R intervals (in seconds)
    rr_intervals = np.diff(r_peaks) / sampling_rate  # R-R intervals in seconds

    # Plot the results
    plt.figure(figsize=(12, 6))
    plt.plot(ecg_signal, label="ECG Signal")
    plt.plot(filtered_signal, label="Filtered ECG", linewidth=1)
    plt.plot(r_peaks, ecg_signal[r_peaks], 'ro', label="Detected R-peaks")
    plt.title("ECG Signal with R-peaks Detection")
    plt.xlabel("Samples")
    plt.ylabel("Amplitude")
    plt.legend()
    plt.show()

    return rr_intervals, r_peaks


# Atrial Fibrillation Detection Class
class AtrialFibrillationDetector:

    def __init__(self, rr_intervals, rr_position):
        self.rr_intervals = rr_intervals  # Pass R-R intervals as input
        self.rr_position = rr_position  # Pass corresponding RR positions

    def detect_af(self):
        # 1. Normalize the rr interval values by their mean
        rr_normalized = []
        total = sum(self.rr_intervals)
        mean = total / len(self.rr_intervals)

        af_detection = []

        for rr in self.rr_intervals:
            # Normalize data
            rr_mean = rr / mean
            rr_normalized.append(rr_mean)

            # 2. Evaluate deviations of over 20% of the mean and target them as AF
            if abs(1 - rr_mean) > 0.2:
                af_detection.append(1.0)
            else:
                af_detection.append(0.0)

        # 3. Apply a low-pass filter to remove artifacts or outliers 
        # Replicating the implementation in Android Studio

        alpha = 0.1
        smooth_af_detection = [af_detection[0]]

        count_above_threshold = 0
        af_detected = 0
        threshold = 0.8
        required_count = 10

        for j in range(1, len(af_detection)):
            smoothed_value = alpha * af_detection[j] + (1 - alpha) * smooth_af_detection[j - 1]
            smooth_af_detection.append(smoothed_value)

            if smoothed_value > threshold:
                count_above_threshold += 1

            # 4. Associate the measurement with AF if over 10 rr intervals present AF
            if count_above_threshold >= required_count:
                af_detected = 1
                break

        return af_detected

