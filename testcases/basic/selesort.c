int main() {
    int n, i, j, tmp, a[105];
    scanf("%d", &n);
    for (i = 0; i < n; ++i) {
        scanf("%d", a + i);
        //scanf("%d", &a[i]);
    }
    for (i = 0; i < n; ++i) {
        for (j = i + 1; j < n; ++j) {
            if (a[j] < a[i]) {
                tmp = a[j];
                a[j] = a[i];
                a[i] = tmp;
            }
        }
    }
    for (i = 0; i < n; ++i) {
        printf("%d ", a[i]);
    }
    printf("\n");
    return 0;
}