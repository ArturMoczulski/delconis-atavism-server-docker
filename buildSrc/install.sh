#!/bin/bash

# ANSI escape codes
BOLD_YELLOW="\033[1;33m"
RESET="\033[0m"

# Function to check if the atavism_server ZIP file exists
check_for_atavism_zip() {
    shopt -s nullglob
    atavism_files=(../archives/atavism_server*.zip)
    if [ ${#atavism_files[@]} -gt 0 ]; then
        return 0
    else
        return 1
    fi
}

# Function to check if the agis ZIP file exists
check_for_agis_zip() {
    shopt -s nullglob
    agis_files=(../archives/agis*.zip)
    if [ ${#agis_files[@]} -gt 0 ]; then
        return 0
    else
        return 1
    fi
}

# Check if the ../atavism_server directory is not empty
if [ -d "../atavism_server" ] && [ "$(ls -A ../atavism_server)" ]; then
    atavism_setup_needed=false
else
    atavism_setup_needed=true
fi

# Check if the ../src/lib/atavism directory is not empty
if [ -d "../src/lib/atavism" ] && [ "$(ls -A ../src/lib/atavism)" ]; then
    agis_setup_needed=false
else
    agis_setup_needed=true
fi

# Check if both files are already present at the start
if ! $atavism_setup_needed && ! $agis_setup_needed; then
    exit 0
fi

# Process for Atavism Server
if $atavism_setup_needed; then
    if ! check_for_atavism_zip; then
        # Main loop to prompt the user until the Atavism Server file is found or timeout is reached
        echo "⏰️ ${BOLD_YELLOW}Please place your downloaded ZIP copy of Atavism Server in the archives directory to continue...${RESET}"

        # Timeout after 3 minutes (180 seconds)
        timeout=180
        interval=2
        elapsed=0

        while ! check_for_atavism_zip; do
            if [ $elapsed -ge $timeout ]; then
                echo "❌ Timeout reached. Atavism Server ZIP archive not found in archives directory."
                exit 1
            fi
            sleep $interval
            elapsed=$((elapsed + interval))
        done
    fi
    echo "✅ Atavism Server ZIP archive found: ${atavism_files[0]}"
fi

# Process for AGIS
if $agis_setup_needed; then
    if ! check_for_agis_zip; then
        # Reset the timer for the AGIS ZIP file check
        elapsed=0
        echo "️⏰ ${BOLD_YELLOW}Please place your downloaded ZIP copy of AGIS in the archives directory to continue...${RESET}"

        # Timeout after 3 minutes (180 seconds)
        timeout=180
        interval=2
        elapsed=0

        while ! check_for_agis_zip; do
            if [ $elapsed -ge $timeout ]; then
                echo "❌ Timeout reached. AGIS ZIP archive not found in archives directory."
                exit 1
            fi
            sleep $interval
            elapsed=$((elapsed + interval))
        done
    fi
    echo "✅ AGIS ZIP archive found: ${agis_files[0]}"
fi
