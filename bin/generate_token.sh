openssl rsa -in ~/.ssh/id_rsa -pubout -out /tmp/key.pub
basename $PWD | openssl rsautl -encrypt -inkey /tmp/key.pub -pubin > auth_token.enc
rm /tmp/key.pub
TOKEN=$(base64 auth_token.enc)
#echo $TOKEN
perl -MURI::Escape -e 'print uri_escape($ARGV[0]);' "$TOKEN"
