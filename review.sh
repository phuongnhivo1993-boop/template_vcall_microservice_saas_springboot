#!/bin/bash

PROMPT_FILE="./opencode_review_prompt.txt"

MODELS=(
  "opencode/deepseek-v4-flash-free"
#   "opencode/big-pickle"
#   "opencode/mimo-v2.5-free"
#   "opencode/minimax-m3-free"
#   "opencode/nemotron-3-ultra-free"
)

TIMEOUT=10
PROMPT=$(cat "$PROMPT_FILE")

while true
do
    echo "==================================="
    echo "START NEW CHECK CYCLE"

    for MODEL in "${MODELS[@]}"
    do
        echo "==================================="
        echo "Testing model: $MODEL"

        TMP_LOG=$(mktemp)

        # chạy background
        opencode run --model "$MODEL" "Reply only OK" > "$TMP_LOG" 2>&1 &
        PID=$!

        KILLED=0

        # timeout manual
        for i in $(seq 1 $TIMEOUT)
        do
            if ! ps -p $PID > /dev/null; then
                break
            fi
            sleep 1
        done

        if ps -p $PID > /dev/null; then
            echo "TIMEOUT → killing"
            kill -9 $PID
            KILLED=1
        fi

        wait $PID 2>/dev/null

        OUTPUT=$(cat "$TMP_LOG")
        rm "$TMP_LOG"

        echo "$OUTPUT"

        # ❌ quota check
        if echo "$OUTPUT" | grep -qiE "429|quota|insufficient|rate limit|exceeded"; then
            echo "❌ Model OUT OF FREE: $MODEL"
            continue
        fi

        # ❌ bị kill
        if [ $KILLED -eq 1 ]; then
            echo "❌ Timeout model: $MODEL"
            continue
        fi

        echo "✅ MODEL OK: $MODEL"
        echo "Running full prompt..."

        opencode run --model "$MODEL" "$PROMPT"

        exit 0

    done

    echo "❌ ALL MODELS FAILED"
    echo "Sleeping 5 minutes before retry..."
    sleep 600
done
