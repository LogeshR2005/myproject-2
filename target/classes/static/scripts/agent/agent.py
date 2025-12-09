import time
import socket
import platform
import datetime
import psutil
import requests
import subprocess
import os

# ==============================
# CONFIG
# ==============================

SERVER = "https://myproject2-6ur3.onrender.com"
AGENT_ID = "agent-5"
INTERVAL = 2


# ==============================
# SAFE UTC TIMESTAMP
# ==============================
def now_utc():
    return datetime.datetime.now(datetime.timezone.utc).isoformat()


# ==============================
# OS DETECTION
# ==============================
def get_os_type():
    os_name = platform.system().lower()
    if "windows" in os_name:
        return "windows"
    elif "linux" in os_name or "darwin" in os_name:
        return "linux"
    return "linux"


# ==============================
# HEARTBEAT → SERVER
# ==============================
def send_heartbeat():
    payload = {
        "agentId": AGENT_ID,
        "hostname": socket.gethostname(),
        "cpuUsage": psutil.cpu_percent(),
        "memoryUsage": psutil.virtual_memory().percent,
        "os": platform.platform(),
        "timestamp": now_utc()
    }

    try:
        requests.post(f"{SERVER}/api/agents/{AGENT_ID}/health", json=payload, timeout=5)
        print("HEARTBEAT SENT")
    except Exception as e:
        print("HEARTBEAT FAILED:", e)


# ==============================
# GET COMMAND + LEVEL
# ==============================
def get_command():
    try:
        return requests.get(
            f"{SERVER}/api/admin/command/{AGENT_ID}", timeout=5
        ).json()
    except:
        return {"command": "NONE", "level": "basic"}


# ==============================
# DOWNLOAD SCRIPT (LEVEL AWARE)
# ==============================
def download_script(command, level):
    os_type = get_os_type()
    url = f"{SERVER}/api/agents/{AGENT_ID}/download/{os_type}/{command}/{level}"

    if os_type == "windows":
        file_name = "rollback.ps1" if command == "ROLLBACK" else f"{level}.ps1"
    else:
        file_name = "rollback.sh" if command == "ROLLBACK" else f"{level}.sh"

    print(f"DOWNLOADING SCRIPT: {url}")

    r = requests.get(url, timeout=10)

    if r.status_code != 200 or len(r.content) < 10:
        raise Exception("Invalid or Empty Script Received")

    with open(file_name, "wb") as f:
        f.write(r.content)

    print(f"SCRIPT DOWNLOADED: {file_name}")
    return file_name


# ==============================
# RUN DOWNLOADED SCRIPT
# ==============================
def run_script(file, command, level):
    print(f"\n🚀 EXECUTING → {command} | LEVEL → {level.upper()}")

    if file.endswith(".ps1"):
        result = subprocess.run(
            ["powershell", "-ExecutionPolicy", "Bypass", "-File", file],
            capture_output=True,
            text=True
        )
    else:
        subprocess.run(["chmod", "+x", file])
        result = subprocess.run(
            ["bash", file],
            capture_output=True,
            text=True
        )

    print("\nSCRIPT OUTPUT:\n", result.stdout)
    print("\nSCRIPT ERROR:\n", result.stderr)

    return result.returncode == 0


# ==============================
# SEND ACK TO SERVER
# ==============================
def send_ack(status, message, level):
    payload = {
        "agentId": AGENT_ID,
        "status": status,
        "level": level,
        "message": message,
        "time": now_utc()
    }

    try:
        requests.post(f"{SERVER}/api/admin/{AGENT_ID}/ack", json=payload, timeout=5)
        print("ACK SENT TO SERVER")
    except Exception as e:
        print("ACK FAILED:", e)


# ==============================
# SEND REPORT + LOG
# ==============================
def fetch_and_send_report(command, level):
    os_type = get_os_type()
    base = "C:\\Hardening" if os_type == "windows" else "/var/lib/hardening-agent"
    os.makedirs(base, exist_ok=True)

    if command == "ROLLBACK":
        report_file = "rollback_report.txt"
        log_file = "rollback.log"
    else:
        report_file = f"{level}_report.txt"
        log_file = f"{level}.log"

    report_path = os.path.join(base, report_file)
    log_path = os.path.join(base, log_file)

    try:
        report_data = open(report_path).read() if os.path.exists(report_path) else "NO REPORT FILE"
        log_data = open(log_path).read() if os.path.exists(log_path) else "NO LOG FILE"

        payload = {
            "agentId": AGENT_ID,
            "command": command,
            "level": level,
            "report": report_data,
            "log": log_data,
            "timestamp": now_utc()
        }

        requests.post(f"{SERVER}/api/admin/{AGENT_ID}/report", json=payload, timeout=10)
        print("REPORT & LOG SENT TO SERVER")

    except Exception as e:
        print("REPORT UPLOAD FAILED:", e)


# ==============================
# MAIN AGENT LOOP (FIXED)
# ==============================
def main():
    print("ENTERPRISE AGENT STARTED")
    print("OS:", get_os_type())
    print("AGENT ID:", AGENT_ID)

    while True:
        send_heartbeat()

        try:
            command_data = get_command()
            cmd = command_data.get("command", "NONE")
            level = command_data.get("level", "basic")

            if cmd != "NONE":
                print(f"📡 COMMAND RECEIVED → {cmd} | LEVEL → {level}")

                script = download_script(cmd, level)
                success = run_script(script, cmd, level)

                if success:
                    send_ack("SUCCESS", f"{cmd} executed", level)
                    fetch_and_send_report(cmd, level)
                else:
                    send_ack("FAILED", f"{cmd} failed", level)

        except Exception as e:
            print("AGENT ERROR:", e)

        time.sleep(INTERVAL)


if __name__ == "__main__":
    main()
