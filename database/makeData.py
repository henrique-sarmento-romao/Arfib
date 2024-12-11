import pandas as pd
import random
import sqlite3
import string
import numpy as np
import os
from datetime import datetime, timedelta
from dateutil.relativedelta import relativedelta

from variables import names, surnames, symptoms, medications

num_Patient = 40
num_Doctor = 10
num_Nurse = 8

# -----------
User = pd.DataFrame({"username":[], "password":[], "first_name":[], "last_name":[], "picture":[]})
Doctor = pd.DataFrame({"username":[]})
Nurse = pd.DataFrame({"username":[]})
Patient = pd.DataFrame({"username":[], "doctor":[], "nurse":[]})
Measurement = pd.DataFrame({"file":[], "date":[], "time":[], "AF_presence":[], "patient":[], "observations":[]})
Symptom = pd.DataFrame({"name":[], "description":[]})
Symptom_Log = pd.DataFrame({"date":[], "time":[], "intensity":[], "patient":[], "symptom":[]})
Medication = pd.DataFrame({"name":[], "effect":[], "image":[]})
Prescription = pd.DataFrame({"patient":[], "medication":[], "frequency":[], "start_date":[], "end_date":[]})
Medication_Log = pd.DataFrame({"patient":[], "medication":[], "date":[], "time":[], "taken":[]})

# -- USER -------------------------------
num_User = num_Patient + num_Doctor + num_Nurse
for i in range(num_User):
    first_name = random.choice(names)
    last_name = random.choice(surnames)

    number = random.randint(0,2)*random.randint(0,49)
    if number == 0: username = first_name.lower() + random.choice([".", "_",""]) + last_name.lower()
    else: username = first_name.lower() + random.choice([".", "_",""]) + last_name.lower() + str(number)

    all_characters = string.ascii_letters + string.digits + string.punctuation
    password = ''.join(random.choices(all_characters, k=12))

    pictures_available = [f for f in os.listdir("database/pictures") if os.path.isfile(os.path.join("database/pictures", f))]
    pictures_available = [os.path.relpath(f,os.getcwd()) for f in pictures_available]
    picture = random.choice(pictures_available)

    User.loc[len(User)] = [username, password, first_name, last_name, picture]


username_list = User["username"].to_list()

# -- DOCTOR -------------------------------
for i in range(num_Doctor):
    username = username_list.pop(random.randint(0, len(username_list)-1))
    Doctor.loc[len(Doctor)] = [username]


# -- NURSE -------------------------------
for i in range(num_Nurse):
    username = username_list.pop(random.randint(0, len(username_list)-1))   
    Nurse.loc[len(Nurse)] = [username] 


# -- PATIENT -------------------------------
doctor_list = Doctor["username"].to_list()
nurse_list = Nurse["username"].to_list()
for i in range(num_Patient):
    username = username_list.pop(random.randint(0, len(username_list)-1)) 
    doctor = random.choice(doctor_list)
    needsNurse = random.random() < 0.25
    if needsNurse == 1:
        nurse = random.choice(doctor_list)
    else:
        nurse = None

    Patient.loc[len(Patient)] = [username, doctor, nurse]

# -- MEASUREMENT -------------------------
ECGs_available = [f for f in os.listdir("database/measurements") if os.path.isfile(os.path.join("database/measurements", f))]
ECGs_available = [os.path.relpath(f,os.getcwd()) for f in ECGs_available]

patient_list = Patient["username"].to_list()
for patient in patient_list:
    for ecg in range(random.randint(8,12)):
        now = datetime.now()
        two_months_ago = now - timedelta(days=60)
        date_time = two_months_ago + (now - two_months_ago) * random.random()
        date = date_time.date()
        time = date_time.time()

        AF_presence = int((random.random() < 0.2))
        file = random.choice(ECGs_available)
        observations = None

        Measurement.loc[len(Measurement)] = [file, date, time, AF_presence, patient, observations]

# -- SYMPTOM ------------------
for sim in symptoms:
    Symptom.loc[len(Symptom)]=[sim[0], sim[1]]

simps = Symptom["name"].to_list()
# -- SYMPTOM LOG ------------------
for patient in patient_list:
    for i in range(random.randint(30,40)):
        now = datetime.now()
        two_months_ago = now - timedelta(days=60)
        date_time = two_months_ago + (now - two_months_ago) * random.random()
        date = date_time.date()
        time = date_time.time()

        intensity = random.randint(1,4)
        symptom = random.choice(simps)
        Symptom_Log.loc[len(Symptom_Log)] = [date, time, intensity, patient, symptom]

#  -- MEDICATIONs ------------------
med_image_list = [f for f in os.listdir("database/medications") if os.path.isfile(os.path.join("database/medications", f))]
med_image_list = [os.path.splitext(os.path.relpath(f, os.getcwd()))[0] for f in med_image_list]
for med in medications:
    med_image = med_image_list.pop(random.randint(0,len(med_image_list)-1))
    Medication.loc[len(Medication)]=[med[0], med[1], med_image]


medication_list = Medication["name"].to_list()
# -- PRESCRIPTION ------------------
for patient in patient_list:
    num_medications = random.choice([2,3,3,3,3,4,5,6])
    medications = random.sample(medication_list, num_medications)

    for med in medications:
        frequency = random.choice([12,24,36,7*24])
        now = datetime.now()
        two_months_ago = now - timedelta(days=60)
        start_date = two_months_ago + (now - two_months_ago) * random.random()
        
        if random.random() < 0.4:
            now = datetime.now()
            two_months_from = now + timedelta(days=60)
            random_time_diff = (two_months_from - now) * random.random()
            end_date = now + random_time_diff
        else:
            end_date = None

        Prescription.loc[len(Prescription)] = [patient, med, frequency, start_date, end_date]


# -- MEDICATION LOG ------------------
for patient in patient_list:
    medications = Prescription[Prescription["patient"] == patient]
    if len(medications) == 0:
        continue
    
    for i in range(len(medications)):  # Looping through all rows
        medication = medications["medication"].iloc[i]  # Use iloc for positional access
        frequency = medications["frequency"].iloc[i]
        
        timestamp = medications["start_date"].iloc[i]
        date_string = timestamp.strftime("%Y-%m-%d %H:%M:%S.%f")
        start_date = datetime.strptime(date_string, "%Y-%m-%d %H:%M:%S.%f")
        start_date = start_date.replace(hour=8, minute=0, second=0, microsecond=0)

        end_date = medications["end_date"].iloc[i]
        if pd.isna(end_date):
            end_date = start_date + relativedelta(months=6)  # Set a default end date
        
        date_time = start_date
        while date_time < end_date:
            date = date_time.date()
            time = date_time.time()
            if date_time > datetime.now():
                taken = 0
            else:
                taken = random.random() > 0.05  # Simulate medication taken with a 5% chance

            Medication_Log.loc[len(Medication_Log)] = [patient, medication, date, time, taken]  # Corrected date_time to date
            date_time += relativedelta(hours=int(frequency))  # Add frequency (in hours)


folder = "database/CSVs/"
User.to_csv(folder+"User.csv", index=False)
Doctor.to_csv(folder+"Doctor.csv", index=False)
Nurse.to_csv(folder+"Nurse.csv", index=False)
Patient.to_csv(folder+"Patient.csv", index=False)
Measurement.to_csv(folder+"Measurement.csv", index=False)
Symptom.to_csv(folder+"Symptom.csv", index=False)
Symptom_Log.to_csv(folder+"Symptom_Log.csv", index=False)
Medication.to_csv(folder+"Medication.csv", index=False)
Prescription.to_csv(folder+"Prescription.csv", index=False)
Medication_Log.to_csv(folder+"Medication_Log.csv", index=False)

conn = sqlite3.connect('database/database.db')
User.to_sql('User', conn, if_exists='replace', index=False)
Doctor.to_sql('Doctor', conn, if_exists='replace', index=False)
Nurse.to_sql('Nurse', conn, if_exists='replace', index=False)
Patient.to_sql('Patient', conn, if_exists='replace', index=False)
Measurement.to_sql('Measurement', conn, if_exists='replace', index=False)
Symptom.to_sql('Symptom', conn, if_exists='replace', index=False)
Symptom_Log.to_sql('Symptom_Log', conn, if_exists='replace', index=False)
Medication.to_sql('Medication', conn, if_exists='replace', index=False)
Prescription.to_sql('Prescription', conn, if_exists='replace', index=False)
Medication_Log.to_sql('Medication_Log', conn, if_exists='replace', index=False)
conn.close()
