openssl rsa -in ~/.ssh/id_rsa -pubout -out /tmp/key.pub
basename $PWD | openssl rsautl -encrypt -inkey /tmp/key.pub -pubin > auth_token.enc
rm /tmp/key.pub
