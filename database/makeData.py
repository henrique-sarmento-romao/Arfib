import pandas as pd
import random
import sqlite3
import string
import numpy as np
import os
from datetime import datetime, timedelta

from variables import names, surnames, symptoms, medications

num_Patient = 40
num_Doctor = 10
num_Nurse = 8

# -----------
User = pd.DataFrame({"username":[], "password":[], "first_name":[], "last_name":[], "picture":[]})
Doctor = pd.DataFrame({"username":[]})
Nurse = pd.DataFrame({"username":[]})
Patient = pd.DataFrame({"username":[], "doctor":[], "nurse":[]})
Measurement = pd.DataFrame({"file":[], "date_time":[], "AF_presence":[], "patient":[]})
Symptom = pd.DataFrame({"name":[], "description":[]})
Symptom_Log = pd.DataFrame({"date_time":[], "intensity":[], "patient":[], "symptom":[]})
Medication = pd.DataFrame({"name":[], "effect":[]})
Prescription = pd.DataFrame({"patient":[], "medication":[], "frequency":[], "start_date":[], "end_date":[]})

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
    needsNurse = random.choice([0,0,0,1])
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

        AF_presence = int((random.random() < 0.2))
        file = None

        Measurement.loc[len(Measurement)] = [file, date_time, AF_presence, patient]

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

        intensity = random.randint(1,4)
        symptom = random.choice(simps)
        Symptom_Log.loc[len(Symptom_Log)] = [date_time, intensity, patient, symptom]

#  -- MEDICATIONs ------------------
for med in medications:
    Medication.loc[len(Medication)]=[med[0], med[1]]

medication_list = Medication["name"].to_list()
# -- PRESCRIPTION ------------------
for patient in patient_list:
    for med in range(random.randint(0,3)):
        medication = random.choice(medication_list)
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

        Prescription.loc[len(Prescription)] = [patient, medication, frequency, start_date, end_date]

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
conn.close()
