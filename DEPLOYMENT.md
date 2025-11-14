# Deployment Guide - Letterboxd Clash

## Railway.app Deployment (Recommended)

### Prerequisites
- GitHub repository with your code
- TMDB API key from https://www.themoviedb.org/settings/api

### Steps

#### 1. Push your code to GitHub
```bash
git add .
git commit -m "Prepare for deployment"
git push origin main
```

#### 2. Deploy Backend (Spring Boot)

1. Go to https://railway.app
2. Click "Start a New Project"
3. Select "Deploy from GitHub repo"
4. Choose your `letterboxdClash` repository
5. Railway will auto-detect it's a Maven project

**Add Environment Variables:**
- Click on your service
- Go to "Variables" tab
- Add variable:
  - Name: `TMDB_API_KEY`
  - Value: `your_tmdb_api_key_here`

**Configure Settings:**
- Railway should auto-detect the build command
- If not, set:
  - Build Command: `./mvnw clean package -DskipTests`
  - Start Command: `java -jar target/letterboxdClash-0.0.1-SNAPSHOT.jar`

Railway will automatically deploy! Your backend will be live at: `https://your-app.railway.app`

#### 3. Deploy Frontend (React)

**Option A: Deploy to Railway as separate service**
1. Click "New" → "GitHub Repo" → Select same repo
2. Set Root Directory: `frontend`
3. Build Command: `npm install && npm run build`
4. Start Command: `npx serve -s build -l $PORT`
5. Add environment variable:
   - Name: `REACT_APP_API_URL`
   - Value: `https://your-backend-url.railway.app`

**Option B: Deploy to Vercel/Netlify (Easier for frontend)**
1. Go to https://vercel.com or https://netlify.com
2. Import your GitHub repo
3. Set build settings:
   - Base directory: `frontend`
   - Build command: `npm run build`
   - Publish directory: `frontend/build`
4. Add environment variable for backend URL

#### 4. Update CORS Configuration

Update `src/main/java/com/lewis/letterboxdClash/config/CorsConfig.java`:

```java
.allowedOrigins(
    "http://localhost:3000",
    "https://your-frontend-url.vercel.app"  // Add your frontend URL
)
```

Commit and push - Railway will auto-deploy the update!

---

## Alternative: All-in-One Deployment

Build React into Spring Boot's static resources for single deployment:

### Steps

1. **Build React:**
```bash
cd frontend
npm run build
```

2. **Copy build to Spring Boot:**
```bash
cp -r build/* ../src/main/resources/static/
```

3. **Update pom.xml** to include frontend in build (optional automation)

4. **Deploy to Railway:**
- Same as backend steps above
- Frontend will be served at same URL as backend

---

## Environment Variables Needed

- `TMDB_API_KEY`: Your TMDB API key (required)
- `PORT`: Railway sets this automatically

---

## Testing Deployment

1. Visit your Railway backend URL
2. Test API: `https://your-app.railway.app/api/hello`
3. If using separate frontend, visit frontend URL
4. If all-in-one, visit backend URL directly

---

## Troubleshooting

**Build fails:**
- Check that Java 21 is being used
- Verify `./mvnw` has execute permissions
- Check Railway build logs

**API errors:**
- Verify `TMDB_API_KEY` environment variable is set
- Check CORS configuration includes your frontend URL

**Blank page:**
- Check browser console for errors
- Verify API URL in React is correct
- Check CORS configuration

---

## Free Tier Limits

**Railway:**
- $5 free credit per month
- Usage-based billing after that
- Hobby plan recommended for production

**Alternative Free Options:**
- fly.io - Free tier with 3 small VMs
- Render.com - 750 hours/month free
