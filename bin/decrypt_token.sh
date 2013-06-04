echo $1 | base64 -D > /tmp/auth_token.enc
DECRYPTED=$(openssl rsautl -decrypt -inkey ~/.ssh/id_rsa -in /tmp/auth_token.enc)
rm /tmp/auth_token.enc
if [[ $2 = $DECRYPTED ]]
then
  echo "valid"
else
  echo "error"
fi
