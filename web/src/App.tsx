import * as React from 'react';
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Link from '@mui/material/Link';
import '@fontsource/roboto/400.css';
import SideMenu from "./SideMenu";

function Copyright() {
  return (
    <Typography
      variant="body2"
      align="center"
      sx={{
        color: 'text.secondary',
      }}
    >
      {'Copyright © '}
      <Link color="inherit" href="https://kss.sh/">
        Kennedy Software Solutions Inc.
      </Link>{' '}
      2024
    </Typography>
  );
}

export default function App() {
  return (
    <Container maxWidth="sm">
      <Box sx={{ my: 4 }}>
        <Typography variant="h4" component="h1" sx={{ mb: 2 }}>
          finmgr
        </Typography>
        <SideMenu />
        <Copyright />
      </Box>
    </Container>
  );
}
